package top.atluofu.middleware.dynamic.config.center.test.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.atluofu.middleware.dynamic.config.center.sdk.template.DdcConfigTemplate;
import top.atluofu.middleware.dynamic.config.center.test.service.DdcDemoService;

import java.util.Map;

@RestController
@RequestMapping("/ddc")
public class DdcDemoController {

    private final DdcDemoService ddcDemoService;
    private final DdcConfigTemplate ddcConfigTemplate;

    public DdcDemoController(DdcDemoService ddcDemoService, DdcConfigTemplate ddcConfigTemplate) {
        this.ddcDemoService = ddcDemoService;
        this.ddcConfigTemplate = ddcConfigTemplate;
    }

    @GetMapping("/values")
    public Map<String, Object> values() {
        return ddcDemoService.currentValues();
    }

    @PostMapping("/publish")
    public Map<String, Object> publish(@RequestParam String attribute, @RequestParam String value) {
        long receivers = ddcConfigTemplate.publish(attribute, value);
        return Map.of("attribute", attribute, "value", value, "receivers", receivers);
    }
}
