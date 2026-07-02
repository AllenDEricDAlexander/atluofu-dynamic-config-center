package top.atluofu.middleware.dynamic.config.center.sdk.listener;

import org.redisson.api.listener.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;
import top.atluofu.middleware.dynamic.config.center.sdk.domain.model.DdcChangeMessage;
import top.atluofu.middleware.dynamic.config.center.sdk.domain.service.IDdcConfigCenterService;
import top.atluofu.middleware.dynamic.config.center.sdk.types.common.DdcConstants;

public class DdcRedisChangeListener implements MessageListener<DdcChangeMessage> {

    private static final Logger log = LoggerFactory.getLogger(DdcRedisChangeListener.class);

    private final IDdcConfigCenterService ddcConfigCenterService;

    public DdcRedisChangeListener(IDdcConfigCenterService ddcConfigCenterService) {
        this.ddcConfigCenterService = ddcConfigCenterService;
    }

    @Override
    public void onMessage(CharSequence channel, DdcChangeMessage message) {
        String oldTraceId = MDC.get(DdcConstants.MDC_TRACE_ID);
        String oldRequestId = MDC.get(DdcConstants.MDC_REQUEST_ID);
        try {
            putMdcIfPresent(DdcConstants.MDC_TRACE_ID, message == null ? null : message.getTraceId());
            putMdcIfPresent(DdcConstants.MDC_REQUEST_ID, message == null ? null : message.getRequestId());
            log.info("DDC received Redis pub/sub message. channel={}, message={}", channel, message);
            ddcConfigCenterService.adjustAttributeValue(message);
        } catch (Exception e) {
            log.error("DDC failed to adjust attribute. channel={}, message={}", channel, message, e);
        } finally {
            restoreMdc(DdcConstants.MDC_TRACE_ID, oldTraceId);
            restoreMdc(DdcConstants.MDC_REQUEST_ID, oldRequestId);
        }
    }

    private static void putMdcIfPresent(String key, String value) {
        if (StringUtils.hasText(value)) {
            MDC.put(key, value);
        }
    }

    private static void restoreMdc(String key, String oldValue) {
        if (oldValue == null) {
            MDC.remove(key);
        } else {
            MDC.put(key, oldValue);
        }
    }
}
