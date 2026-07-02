package top.atluofu.middleware.dynamic.config.center.sdk.domain.service;

import top.atluofu.middleware.dynamic.config.center.sdk.domain.model.DdcChangeMessage;

public interface IDdcConfigCenterService {

    Object bindObject(Object bean, String beanName);

    void adjustAttributeValue(DdcChangeMessage changeMessage);
}
