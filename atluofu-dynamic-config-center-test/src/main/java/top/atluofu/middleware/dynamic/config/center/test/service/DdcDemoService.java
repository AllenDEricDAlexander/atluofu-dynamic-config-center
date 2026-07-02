package top.atluofu.middleware.dynamic.config.center.test.service;

import org.springframework.stereotype.Service;
import top.atluofu.middleware.dynamic.config.center.sdk.annotation.DdcValue;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class DdcDemoService {

    /**
     * Use volatile for simple hot-path reads after reflection update.
     */
    @DdcValue("downgradeSwitch:0")
    private volatile Integer downgradeSwitch;

    @DdcValue("grayRate:0.01")
    private volatile BigDecimal grayRate;

    public Map<String, Object> currentValues() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("downgradeSwitch", downgradeSwitch);
        values.put("grayRate", grayRate);
        return values;
    }
}
