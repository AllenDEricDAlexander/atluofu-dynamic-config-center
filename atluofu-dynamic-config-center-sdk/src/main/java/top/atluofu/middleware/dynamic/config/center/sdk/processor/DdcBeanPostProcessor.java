package top.atluofu.middleware.dynamic.config.center.sdk.processor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import top.atluofu.middleware.dynamic.config.center.sdk.domain.service.IDdcConfigCenterService;

public class DdcBeanPostProcessor implements BeanPostProcessor, Ordered {

    private final IDdcConfigCenterService ddcConfigCenterService;

    public DdcBeanPostProcessor(IDdcConfigCenterService ddcConfigCenterService) {
        this.ddcConfigCenterService = ddcConfigCenterService;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return ddcConfigCenterService.bindObject(bean, beanName);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
