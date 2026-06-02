package cn.ggsn.openrxlight.transaction.pay.wechat.domain;

import cn.ggsn.openrxlight.errorx.ErrorCode;
import lombok.Getter;

@Getter
public enum WechatErrorCode implements ErrorCode {
    SIGN_ERROR("签名错误", 1),
    CHANNEL_ERROR("渠道错误, 错误码：%s, 错误信息：%s", 2),
    ;

    private final String message;
    private final int value;

    WechatErrorCode(String message, int value) {
        this.message = message;
        this.value = value;
    }
}
