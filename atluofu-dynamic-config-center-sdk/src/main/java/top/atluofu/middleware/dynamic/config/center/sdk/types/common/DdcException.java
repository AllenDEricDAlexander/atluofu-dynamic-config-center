package top.atluofu.middleware.dynamic.config.center.sdk.types.common;

public class DdcException extends RuntimeException {

    public DdcException(String message) {
        super(message);
    }

    public DdcException(String message, Throwable cause) {
        super(message, cause);
    }
}
