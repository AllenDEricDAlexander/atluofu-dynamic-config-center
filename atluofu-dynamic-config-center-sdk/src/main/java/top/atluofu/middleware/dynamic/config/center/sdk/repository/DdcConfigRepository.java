package top.atluofu.middleware.dynamic.config.center.sdk.repository;

import java.util.Optional;

public interface DdcConfigRepository {

    Optional<String> get(String key);

    boolean exists(String key);

    void set(String key, String value);

    boolean setIfAbsent(String key, String value);
}
