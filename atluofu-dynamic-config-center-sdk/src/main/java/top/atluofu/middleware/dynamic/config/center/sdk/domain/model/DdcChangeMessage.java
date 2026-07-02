package top.atluofu.middleware.dynamic.config.center.sdk.domain.model;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

public class DdcChangeMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String system;
    private String namespace;
    private String attribute;
    private String value;
    private String operator;
    private String traceId;
    private String requestId;
    private Long timestamp;

    public DdcChangeMessage() {
    }

    public DdcChangeMessage(String system, String namespace, String attribute, String value) {
        this.system = system;
        this.namespace = namespace;
        this.attribute = attribute;
        this.value = value;
        this.timestamp = Instant.now().toEpochMilli();
    }

    public static DdcChangeMessage of(String system, String namespace, String attribute, String value) {
        return new DdcChangeMessage(system, namespace, attribute, value);
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "DdcChangeMessage{" +
                "system='" + system + '\'' +
                ", namespace='" + namespace + '\'' +
                ", attribute='" + attribute + '\'' +
                ", value='" + value + '\'' +
                ", operator='" + operator + '\'' +
                ", traceId='" + traceId + '\'' +
                ", requestId='" + requestId + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DdcChangeMessage that)) {
            return false;
        }
        return Objects.equals(system, that.system)
                && Objects.equals(namespace, that.namespace)
                && Objects.equals(attribute, that.attribute)
                && Objects.equals(value, that.value)
                && Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(system, namespace, attribute, value, timestamp);
    }
}
