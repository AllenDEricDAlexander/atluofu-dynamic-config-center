package top.atluofu.middleware.dynamic.config.center.sdk.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;
import top.atluofu.middleware.dynamic.config.center.sdk.types.common.DdcConstants;

/**
 * AtLuoFu DDC configuration properties.
 */
@ConfigurationProperties(prefix = "atluofu.dynamic.config.center")
public class DdcProperties {

    /**
     * Whether to enable DDC starter.
     */
    private boolean enabled = true;

    /**
     * Application/system name. Used for Redis key isolation.
     */
    private String system = DdcConstants.DEFAULT_SYSTEM;

    /**
     * Namespace, such as dev/test/prod or tenant group.
     */
    private String namespace = DdcConstants.DEFAULT_NAMESPACE;

    /**
     * Redis key prefix.
     */
    private String keyPrefix = DdcConstants.DEFAULT_KEY_PREFIX;

    /**
     * Redis pub/sub topic. Empty means auto build by keyPrefix/system/namespace.
     */
    private String topic;

    /**
     * Create Redis key with annotation default value when the key does not exist.
     */
    private boolean createMissing = true;

    /**
     * Throw exception when a config key is missing and no default value exists.
     */
    private boolean failFast = true;

    /**
     * Redis client settings used only when application does not provide RedissonClient bean.
     */
    private Redis redis = new Redis();

    public String buildConfigKey(String attribute) {
        return normalizePart(keyPrefix, DdcConstants.DEFAULT_KEY_PREFIX)
                + DdcConstants.VALUE_SEPARATOR + normalizePart(system, DdcConstants.DEFAULT_SYSTEM)
                + DdcConstants.VALUE_SEPARATOR + normalizePart(namespace, DdcConstants.DEFAULT_NAMESPACE)
                + DdcConstants.VALUE_SEPARATOR + attribute;
    }

    public String buildTopic() {
        if (StringUtils.hasText(topic)) {
            return topic.trim();
        }
        return normalizePart(keyPrefix, DdcConstants.DEFAULT_KEY_PREFIX)
                + DdcConstants.VALUE_SEPARATOR + normalizePart(system, DdcConstants.DEFAULT_SYSTEM)
                + DdcConstants.VALUE_SEPARATOR + normalizePart(namespace, DdcConstants.DEFAULT_NAMESPACE)
                + DdcConstants.VALUE_SEPARATOR + DdcConstants.DEFAULT_TOPIC_SUFFIX;
    }

    private static String normalizePart(String value, String defaultValue) {
        String candidate = StringUtils.hasText(value) ? value.trim() : defaultValue;
        while (candidate.endsWith(DdcConstants.VALUE_SEPARATOR)) {
            candidate = candidate.substring(0, candidate.length() - 1);
        }
        while (candidate.startsWith(DdcConstants.VALUE_SEPARATOR)) {
            candidate = candidate.substring(1);
        }
        return candidate;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public boolean isCreateMissing() {
        return createMissing;
    }

    public void setCreateMissing(boolean createMissing) {
        this.createMissing = createMissing;
    }

    public boolean isFailFast() {
        return failFast;
    }

    public void setFailFast(boolean failFast) {
        this.failFast = failFast;
    }

    public Redis getRedis() {
        return redis;
    }

    public void setRedis(Redis redis) {
        this.redis = redis;
    }

    public static class Redis {

        /**
         * Redisson single server address, for example redis://127.0.0.1:6379.
         */
        private String address = "redis://127.0.0.1:6379";

        private String password;

        private int database = 0;

        private int connectionPoolSize = 16;

        private int connectionMinimumIdleSize = 4;

        private int timeout = 3000;

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public int getDatabase() {
            return database;
        }

        public void setDatabase(int database) {
            this.database = database;
        }

        public int getConnectionPoolSize() {
            return connectionPoolSize;
        }

        public void setConnectionPoolSize(int connectionPoolSize) {
            this.connectionPoolSize = connectionPoolSize;
        }

        public int getConnectionMinimumIdleSize() {
            return connectionMinimumIdleSize;
        }

        public void setConnectionMinimumIdleSize(int connectionMinimumIdleSize) {
            this.connectionMinimumIdleSize = connectionMinimumIdleSize;
        }

        public int getTimeout() {
            return timeout;
        }

        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }
    }
}
