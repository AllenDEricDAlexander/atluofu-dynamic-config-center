package top.atluofu.middleware.dynamic.config.center.sdk.domain.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.util.StringUtils;
import top.atluofu.middleware.dynamic.config.center.sdk.annotation.DdcValue;
import top.atluofu.middleware.dynamic.config.center.sdk.config.DdcProperties;
import top.atluofu.middleware.dynamic.config.center.sdk.domain.model.DdcChangeMessage;
import top.atluofu.middleware.dynamic.config.center.sdk.domain.model.DdcFieldBinding;
import top.atluofu.middleware.dynamic.config.center.sdk.domain.model.DdcValueDefinition;
import top.atluofu.middleware.dynamic.config.center.sdk.repository.DdcConfigRepository;
import top.atluofu.middleware.dynamic.config.center.sdk.types.common.DdcException;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class DdcConfigCenterService implements IDdcConfigCenterService {

    private static final Logger log = LoggerFactory.getLogger(DdcConfigCenterService.class);

    private final DdcProperties properties;
    private final DdcConfigRepository repository;
    private final ConversionService conversionService;
    private final ConcurrentMap<String, CopyOnWriteArrayList<DdcFieldBinding>> bindings = new ConcurrentHashMap<>();

    public DdcConfigCenterService(DdcProperties properties,
                                  DdcConfigRepository repository,
                                  ConversionService conversionService) {
        this.properties = properties;
        this.repository = repository;
        this.conversionService = conversionService;
    }

    @Override
    public Object bindObject(Object bean, String beanName) {
        if (bean == null) {
            return null;
        }
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        Object targetBean = resolveTargetObject(bean);
        if (!targetClass.isInstance(targetBean)) {
            log.warn("Skip @DdcValue bind because target object cannot be resolved. beanName={}, proxyClass={}, targetClass={}",
                    beanName, bean.getClass().getName(), targetClass.getName());
            return bean;
        }

        List<Field> ddcFields = findDdcFields(targetClass);
        if (ddcFields.isEmpty()) {
            return bean;
        }

        for (Field field : ddcFields) {
            bindField(beanName, targetBean, field);
        }
        return bean;
    }

    @Override
    public void adjustAttributeValue(DdcChangeMessage changeMessage) {
        validateMessageScope(changeMessage);
        String attribute = normalizeAttribute(changeMessage.getAttribute());
        String configKey = properties.buildConfigKey(attribute);
        String newValue = changeMessage.getValue();

        repository.set(configKey, newValue);

        List<DdcFieldBinding> matchedBindings = bindings.get(configKey);
        if (matchedBindings == null || matchedBindings.isEmpty()) {
            log.info("DDC changed Redis config but no local bean field is bound. configKey={}, value={}", configKey, newValue);
            return;
        }

        for (DdcFieldBinding binding : matchedBindings) {
            binding.setValue(newValue, conversionService);
        }
        log.info("DDC refreshed local bean fields. configKey={}, value={}, count={}", configKey, newValue, matchedBindings.size());
    }

    private void bindField(String beanName, Object targetBean, Field field) {
        if (Modifier.isStatic(field.getModifiers())) {
            log.warn("Skip static @DdcValue field. beanName={}, field={}", beanName, field.getName());
            return;
        }
        DdcValue ddcValue = field.getAnnotation(DdcValue.class);
        DdcValueDefinition definition = DdcValueDefinition.parse(ddcValue.value(), field.getName());
        String attribute = normalizeAttribute(definition.getAttribute());
        String configKey = properties.buildConfigKey(attribute);

        String initValue = getOrCreateConfigValue(configKey, definition, beanName, field);
        DdcFieldBinding binding = new DdcFieldBinding(beanName, targetBean, field, attribute, configKey);
        binding.setValue(initValue, conversionService);

        bindings.computeIfAbsent(configKey, ignored -> new CopyOnWriteArrayList<>()).add(binding);
        log.info("DDC bound field. beanName={}, field={}, attribute={}, configKey={}, initValue={}",
                beanName, field.getName(), attribute, configKey, initValue);
    }

    private String getOrCreateConfigValue(String configKey, DdcValueDefinition definition, String beanName, Field field) {
        Optional<String> redisValue = repository.get(configKey);
        if (redisValue.isPresent()) {
            return redisValue.get();
        }
        if (definition.hasDefaultValue() && properties.isCreateMissing()) {
            repository.setIfAbsent(configKey, definition.getDefaultValue());
            return repository.get(configKey).orElse(definition.getDefaultValue());
        }
        if (definition.hasDefaultValue()) {
            return definition.getDefaultValue();
        }
        String message = "DDC config key does not exist and @DdcValue has no default value. "
                + "beanName=" + beanName
                + ", field=" + field.getName()
                + ", configKey=" + configKey;
        if (properties.isFailFast()) {
            throw new DdcException(message);
        }
        log.warn(message);
        return null;
    }

    private void validateMessageScope(DdcChangeMessage message) {
        if (message == null) {
            throw new DdcException("DDC change message must not be null");
        }
        if (!StringUtils.hasText(message.getAttribute())) {
            throw new DdcException("DDC change message attribute must not be blank");
        }
        if (StringUtils.hasText(message.getSystem()) && !Objects.equals(message.getSystem(), properties.getSystem())) {
            throw new DdcException("DDC message system mismatch. messageSystem=" + message.getSystem()
                    + ", currentSystem=" + properties.getSystem());
        }
        if (StringUtils.hasText(message.getNamespace()) && !Objects.equals(message.getNamespace(), properties.getNamespace())) {
            throw new DdcException("DDC message namespace mismatch. messageNamespace=" + message.getNamespace()
                    + ", currentNamespace=" + properties.getNamespace());
        }
    }

    private static String normalizeAttribute(String attribute) {
        if (!StringUtils.hasText(attribute)) {
            throw new DdcException("DDC attribute must not be blank");
        }
        return attribute.trim();
    }

    private static Object resolveTargetObject(Object bean) {
        if (!AopUtils.isAopProxy(bean)) {
            return bean;
        }
        Object singletonTarget = AopProxyUtils.getSingletonTarget(bean);
        return singletonTarget == null ? bean : singletonTarget;
    }

    private static List<Field> findDdcFields(Class<?> targetClass) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = targetClass;
        while (current != null && !Object.class.equals(current)) {
            for (Field field : current.getDeclaredFields()) {
                if (field.isAnnotationPresent(DdcValue.class)) {
                    fields.add(field);
                }
            }
            current = current.getSuperclass();
        }
        return fields;
    }
}
