package top.atluofu.middleware.dynamic.config.center.test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.atluofu.middleware.dynamic.config.center.sdk.template.DdcConfigTemplate;
import top.atluofu.middleware.dynamic.config.center.test.service.DdcDemoService;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class DdcConfigCenterTest {

    @Autowired
    private DdcDemoService ddcDemoService;

    @Autowired
    private DdcConfigTemplate ddcConfigTemplate;

    @Test
    void testGetInjectedValue() {
        System.out.println(ddcDemoService.currentValues());
    }

    @Test
    void testPublishChange() throws InterruptedException {
        ddcConfigTemplate.publish("downgradeSwitch", 4);
        TimeUnit.MILLISECONDS.sleep(300);
        System.out.println(ddcDemoService.currentValues());
    }

    @Test
    void testPublishBatchChange() throws InterruptedException {
        ddcConfigTemplate.publishAll(Map.of(
                "downgradeSwitch", 1,
                "grayRate", "0.25"
        ));
        TimeUnit.MILLISECONDS.sleep(300);
        System.out.println(ddcDemoService.currentValues());
    }
}
