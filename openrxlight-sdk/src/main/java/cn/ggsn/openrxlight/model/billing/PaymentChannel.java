package cn.ggsn.openrxlight.model.billing;

import com.fasterxml.jackson.databind.annotation.EnumNaming;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@EnumNaming(com.fasterxml.jackson.databind.EnumNamingStrategies.LowerCaseStrategy.class)
@RequiredArgsConstructor
@Getter
public enum PaymentChannel {
    WECHAT((short) 1, "微信支付"),
    ALIPAY((short) 2, "支付宝支付"),
    ;

    private final short value;
    private final String name;

    public static PaymentChannel fromString(String paymentChannel) {
        for (PaymentChannel channel : PaymentChannel.values()) {
            if (channel.name().equalsIgnoreCase(paymentChannel)) {
                return channel;
            }
        }
        throw new IllegalArgumentException("Unsupported payment channel: " + paymentChannel);
    }

    public static PaymentChannel fromValue(Short paymentChannel) {
        for (PaymentChannel channel : PaymentChannel.values()) {
            if (channel.getValue() == paymentChannel) {
                return channel;
            }
        }
        throw new IllegalArgumentException("Unsupported payment channel: " + paymentChannel);
    }
}
