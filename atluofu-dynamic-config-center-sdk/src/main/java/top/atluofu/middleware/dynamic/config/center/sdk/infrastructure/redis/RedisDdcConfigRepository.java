package top.atluofu.middleware.dynamic.config.center.sdk.infrastructure.redis;

import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import top.atluofu.middleware.dynamic.config.center.sdk.repository.DdcConfigRepository;

import java.util.Optional;

public class RedisDdcConfigRepository implements DdcConfigRepository {

    private final RedissonClient redissonClient;

    public RedisDdcConfigRepository(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public Optional<String> get(String key) {
        RBucket<String> bucket = redissonClient.getBucket(key);
        return Optional.ofNullable(bucket.get());
    }

    @Override
    public boolean exists(String key) {
        RBucket<String> bucket = redissonClient.getBucket(key);
        return bucket.isExists();
    }

    @Override
    public void set(String key, String value) {
        RBucket<String> bucket = redissonClient.getBucket(key);
        bucket.set(value);
    }

    @Override
    public boolean setIfAbsent(String key, String value) {
        RBucket<String> bucket = redissonClient.getBucket(key);
        return bucket.trySet(value);
    }
}
