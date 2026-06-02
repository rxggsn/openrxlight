package cn.ggsn.openrxlight.model.order;

import java.util.Objects;

import lombok.Getter;

public enum PayType {
    UNSPECIFIED(0, "未知"),
    WALLET(1, "余额支付"),
    PWD_FREE(4, "免密支付"),

    PRE_FEE(5, "预支付"),
    VIP(7, "充电卡/会员卡支付"),
    OTHER(99, "其他支付方式");

    @Getter
    private final int value;

    @Getter
    private final String message;

    PayType(int value, String message) {
        this.value = value;
        this.message = message;

    }

    public static PayType fromValue(int payType) {
        for (PayType type : PayType.values()) {
            if (type.value == payType) {
                return type;
            }
        }
        return UNSPECIFIED;
    }

    // 主要用于判断结束订单的方式
    public static PayType fromValue(int payType, Integer subPayType) {
        for (PayType type : PayType.values()) {
            if (Objects.nonNull(subPayType)) {
                if (subPayType == UNSPECIFIED.value) {
                    return UNSPECIFIED;
                }

                return fromValue(subPayType);
            } else if (type.value == payType) {
                return type;
            }
        }
        return UNSPECIFIED;
    }
}
