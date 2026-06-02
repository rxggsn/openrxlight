package cn.ggsn.openrxlight.transaction.pay.wechat.domain;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

public enum WechatEventType {
    UNDEFINED("UNDEFINED"),
    PAYSCORE_USER_OPEN_SERVICE("PAYSCORE.USER.OPENSERVICE"),
    PAYSCORE_USER_CLOSE_SERVICE("PAYSCORE.USER.CLOSESERVICE"),
    PAYSCORE_USER_CONFIRM("PAYSCORE.USER_CONFIRM"),
    PAYSCORE_USER_PAID("PAYSCORE.USER_PAID"),
    TRANSACTION_SUCCESS("TRANSACTION.SUCCESS"),
    REFUND_SUCCESS("REFUND.SUCCESS"),
    REFUND_ABNORMAL("REFUND.ABNORMAL"),
    REFUND_CLOSED("REFUND.CLOSED"),
    ;
    @Getter
    private final String value;

    WechatEventType(String value) {
        this.value = value;
    }

    public static WechatEventType fromValue(String value) {
        for (WechatEventType eventType : WechatEventType.values()) {
            if (StringUtils.equals(eventType.getValue(), value)) {
                return eventType;
            }
        }
        return UNDEFINED;
    }
}
