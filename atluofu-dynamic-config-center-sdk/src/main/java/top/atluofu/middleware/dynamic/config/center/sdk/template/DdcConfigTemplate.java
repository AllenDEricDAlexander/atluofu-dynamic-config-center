package top.atluofu.middleware.dynamic.config.center.sdk.template;

import org.redisson.api.RTopic;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;
import top.atluofu.middleware.dynamic.config.center.sdk.config.DdcProperties;
import top.atluofu.middleware.dynamic.config.center.sdk.domain.model.DdcChangeMessage;
import top.atluofu.middleware.dynamic.config.center.sdk.repository.DdcConfigRepository;
import top.atluofu.middleware.dynamic.config.center.sdk.types.common.DdcConstants;
import top.atluofu.middleware.dynamic.config.center.sdk.types.common.DdcException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class DdcConfigTemplate {

    private final DdcProperties properties;
    private final DdcConfigRepository repository;
    private final RTopic topic;

    public DdcConfigTemplate(DdcProperties properties, DdcConfigRepository repository, RTopic topic) {
        this.properties = properties;
        this.repository = repository;
        this.topic = topic;
    }

    public Optional<String> get(String attribute) {
        return repository.get(properties.buildConfigKey(normalizeAttribute(attribute)));
    }

    public void set(String attribute, Object value) {
        repository.set(properties.buildConfigKey(normalizeAttribute(attribute)), stringify(value));
    }

    public long publish(String attribute, Object value) {
        DdcChangeMessage message = buildMessage(attribute, value);
        repository.set(properties.buildConfigKey(message.getAttribute()), message.getValue());
        return topic.publish(message);
    }

    public Map<String, Long> publishAll(Map<String, ?> changes) {
        if (changes == null || changes.isEmpty()) {
            return Map.of();
        }
        Map<String, Long> result = new LinkedHashMap<>();
        for (Map.Entry<String, ?> entry : changes.entrySet()) {
            result.put(entry.getKey(), publish(entry.getKey(), entry.getValue()));
        }
        return result;
    }

    private DdcChangeMessage buildMessage(String attribute, Object value) {
        DdcChangeMessage message = DdcChangeMessage.of(
                properties.getSystem(),
                properties.getNamespace(),
                normalizeAttribute(attribute),
                stringify(value));
        message.setTimestamp(Instant.now().toEpochMilli());
        message.setTraceId(MDC.get(DdcConstants.MDC_TRACE_ID));
        message.setRequestId(MDC.get(DdcConstants.MDC_REQUEST_ID));
        return message;
    }

    private static String normalizeAttribute(String attribute) {
        if (!StringUtils.hasText(attribute)) {
            throw new DdcException("DDC attribute must not be blank");
        }
        return attribute.trim();
    }

    private static String stringify(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
