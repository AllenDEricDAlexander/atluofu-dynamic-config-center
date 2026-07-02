package top.atluofu.middleware.dynamic.config.center.sdk.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Dynamic distributed config value.
 *
 * <p>Usage: {@code @DdcValue("downgradeSwitch:0")}</p>
 * <p>Format: {@code attribute:defaultValue}. The default value is written to Redis only when the key does not exist.</p>
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DdcValue {

    /**
     * Config expression, for example: {@code downgradeSwitch:0}.
     */
    String value();
}
