package top.atluofu.middleware.dynamic.config.center.sdk.config;

import org.redisson.Redisson;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.util.StringUtils;
import top.atluofu.middleware.dynamic.config.center.sdk.domain.model.DdcChangeMessage;
import top.atluofu.middleware.dynamic.config.center.sdk.domain.service.DdcConfigCenterService;
import top.atluofu.middleware.dynamic.config.center.sdk.domain.service.IDdcConfigCenterService;
import top.atluofu.middleware.dynamic.config.center.sdk.infrastructure.redis.RedisDdcConfigRepository;
import top.atluofu.middleware.dynamic.config.center.sdk.listener.DdcRedisChangeListener;
import top.atluofu.middleware.dynamic.config.center.sdk.processor.DdcBeanPostProcessor;
import top.atluofu.middleware.dynamic.config.center.sdk.repository.DdcConfigRepository;
import top.atluofu.middleware.dynamic.config.center.sdk.template.DdcConfigTemplate;

@AutoConfiguration
@EnableConfigurationProperties(DdcProperties.class)
@ConditionalOnProperty(prefix = "atluofu.dynamic.config.center", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DdcAutoConfiguration {

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean(RedissonClient.class)
    public RedissonClient atluofuDdcRedissonClient(DdcProperties properties) {
        Config config = new Config();
        config.setCodec(JsonJacksonCodec.INSTANCE);
        SingleServerConfig singleServerConfig = config.useSingleServer()
                .setAddress(normalizeRedisAddress(properties.getRedis().getAddress()))
                .setDatabase(properties.getRedis().getDatabase())
                .setConnectionPoolSize(properties.getRedis().getConnectionPoolSize())
                .setConnectionMinimumIdleSize(properties.getRedis().getConnectionMinimumIdleSize())
                .setTimeout(properties.getRedis().getTimeout());
        if (StringUtils.hasText(properties.getRedis().getPassword())) {
            singleServerConfig.setPassword(properties.getRedis().getPassword());
        }
        return Redisson.create(config);
    }

    @Bean
    @ConditionalOnMissingBean(DdcConfigRepository.class)
    @ConditionalOnBean(RedissonClient.class)
    public DdcConfigRepository ddcConfigRepository(RedissonClient redissonClient) {
        return new RedisDdcConfigRepository(redissonClient);
    }

    @Bean(name = "ddcConversionService")
    @ConditionalOnMissingBean(name = "ddcConversionService")
    public ConversionService ddcConversionService() {
        return DefaultConversionService.getSharedInstance();
    }

    @Bean
    @ConditionalOnMissingBean(IDdcConfigCenterService.class)
    public IDdcConfigCenterService ddcConfigCenterService(DdcProperties properties,
                                                          DdcConfigRepository repository,
                                                          @Qualifier("ddcConversionService") ConversionService conversionService) {
        return new DdcConfigCenterService(properties, repository, conversionService);
    }

    @Bean
    @ConditionalOnMissingBean(DdcRedisChangeListener.class)
    public DdcRedisChangeListener ddcRedisChangeListener(IDdcConfigCenterService ddcConfigCenterService) {
        return new DdcRedisChangeListener(ddcConfigCenterService);
    }

    @Bean(name = "atluofuDdcRedisTopic")
    @ConditionalOnMissingBean(name = "atluofuDdcRedisTopic")
    public RTopic atluofuDdcRedisTopic(DdcProperties properties,
                                       RedissonClient redissonClient,
                                       DdcRedisChangeListener listener) {
        RTopic topic = redissonClient.getTopic(properties.buildTopic());
        topic.addListener(DdcChangeMessage.class, listener);
        return topic;
    }

    @Bean
    @ConditionalOnMissingBean(DdcConfigTemplate.class)
    public DdcConfigTemplate ddcConfigTemplate(DdcProperties properties,
                                               DdcConfigRepository repository,
                                               @Qualifier("atluofuDdcRedisTopic") RTopic topic) {
        return new DdcConfigTemplate(properties, repository, topic);
    }

    @Bean
    @ConditionalOnMissingBean(DdcBeanPostProcessor.class)
    public DdcBeanPostProcessor ddcBeanPostProcessor(IDdcConfigCenterService ddcConfigCenterService) {
        return new DdcBeanPostProcessor(ddcConfigCenterService);
    }

    private static String normalizeRedisAddress(String address) {
        if (!StringUtils.hasText(address)) {
            return "redis://127.0.0.1:6379";
        }
        String trimmed = address.trim();
        if (trimmed.startsWith("redis://") || trimmed.startsWith("rediss://")) {
            return trimmed;
        }
        return "redis://" + trimmed;
    }
}
