package cn.ggsn.openrxlight.account.external.impl.wechat.error;

/**
 * WxJava专用的runtime exception.
 *
 * @author <a href="https://github.com/binarywang">Binary Wang</a>
 * @date 2020-09-26
 */
public class WxRuntimeException extends RuntimeException {
    public WxRuntimeException(Throwable e) {
        super(e);
    }

    public WxRuntimeException(String msg) {
        super(msg);
    }

    public WxRuntimeException(String msg, Throwable e) {
        super(msg, e);
    }
}
