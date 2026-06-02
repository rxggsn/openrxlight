package cn.ggsn.openrxlight.transaction.domain;

import com.fasterxml.jackson.databind.annotation.EnumNaming;

import lombok.Getter;

@EnumNaming(com.fasterxml.jackson.databind.EnumNamingStrategies.SnakeCaseStrategy.class)
public enum ChannelType {
    UNSPECIFIED(0),
    WECHAT_OFFICIAL_PAY(1), // 微信官方支付渠道
    WALLET(2), // 钱包支付渠道，适用于内部钱包系统
    FUIOU_PAY(6), // 富友统一支付渠道
    ;

    @Getter
    private final int value;

    ChannelType(int value) {
        this.value = value;
    }

    public static ChannelType fromValue(int value) {
        for (ChannelType type : ChannelType.values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        return UNSPECIFIED;
    }
}
