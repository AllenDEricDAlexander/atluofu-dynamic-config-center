package top.atluofu.middleware.dynamic.config.center.sdk.domain.model;

import org.springframework.core.convert.ConversionService;
import org.springframework.util.ReflectionUtils;
import top.atluofu.middleware.dynamic.config.center.sdk.types.common.DdcException;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicReference;

public final class DdcFieldBinding {

    private final String beanName;
    private final Object targetBean;
    private final Field field;
    private final String attribute;
    private final String configKey;

    public DdcFieldBinding(String beanName, Object targetBean, Field field, String attribute, String configKey) {
        if (Modifier.isStatic(field.getModifiers())) {
            throw new DdcException("@DdcValue cannot be used on static field, field=" + field.getName());
        }
        if (Modifier.isFinal(field.getModifiers())) {
            throw new DdcException("@DdcValue cannot be used on final field, field=" + field.getName());
        }
        this.beanName = beanName;
        this.targetBean = targetBean;
        this.field = field;
        this.attribute = attribute;
        this.configKey = configKey;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void setValue(String rawValue, ConversionService conversionService) {
        try {
            ReflectionUtils.makeAccessible(field);
            Class<?> fieldType = field.getType();
            if (AtomicReference.class.isAssignableFrom(fieldType)) {
                AtomicReference reference = (AtomicReference) field.get(targetBean);
                if (reference == null) {
                    reference = new AtomicReference(rawValue);
                    field.set(targetBean, reference);
                } else {
                    reference.set(rawValue);
                }
                return;
            }
            Object convertedValue = convertValue(rawValue, fieldType, conversionService);
            field.set(targetBean, convertedValue);
        } catch (Exception e) {
            throw new DdcException("Failed to set @DdcValue field, bean=" + beanName
                    + ", field=" + field.getName()
                    + ", attribute=" + attribute
                    + ", configKey=" + configKey
                    + ", rawValue=" + rawValue, e);
        }
    }

    private Object convertValue(String rawValue, Class<?> fieldType, ConversionService conversionService) {
        if (rawValue == null) {
            if (fieldType.isPrimitive()) {
                throw new DdcException("Cannot set null to primitive field, field=" + field.getName());
            }
            return null;
        }
        if (String.class.equals(fieldType)) {
            return rawValue;
        }
        if (conversionService.canConvert(String.class, fieldType)) {
            return conversionService.convert(rawValue, fieldType);
        }
        throw new DdcException("No converter from String to " + fieldType.getName()
                + ", field=" + field.getName()
                + ". Please use String field or register a Converter.");
    }

    public String getBeanName() {
        return beanName;
    }

    public Object getTargetBean() {
        return targetBean;
    }

    public Field getField() {
        return field;
    }

    public String getAttribute() {
        return attribute;
    }

    public String getConfigKey() {
        return configKey;
    }
}
