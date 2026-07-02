package top.atluofu.middleware.dynamic.config.center.sdk.domain.model;

import org.springframework.util.StringUtils;
import top.atluofu.middleware.dynamic.config.center.sdk.types.common.DdcConstants;
import top.atluofu.middleware.dynamic.config.center.sdk.types.common.DdcException;

import java.util.regex.Pattern;

public final class DdcValueDefinition {

    private final String attribute;
    private final String defaultValue;

    private DdcValueDefinition(String attribute, String defaultValue) {
        this.attribute = attribute;
        this.defaultValue = defaultValue;
    }

    public static DdcValueDefinition parse(String expression, String fieldName) {
        if (!StringUtils.hasText(expression)) {
            throw new DdcException("@DdcValue expression must not be blank, field=" + fieldName);
        }
        String[] parts = expression.split(Pattern.quote(DdcConstants.VALUE_SEPARATOR), 2);
        String attribute = parts[0].trim();
        if (!StringUtils.hasText(attribute)) {
            throw new DdcException("@DdcValue attribute must not be blank, field=" + fieldName + ", expression=" + expression);
        }
        String defaultValue = parts.length > 1 ? parts[1] : null;
        return new DdcValueDefinition(attribute, defaultValue);
    }

    public String getAttribute() {
        return attribute;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public boolean hasDefaultValue() {
        return defaultValue != null;
    }
}
