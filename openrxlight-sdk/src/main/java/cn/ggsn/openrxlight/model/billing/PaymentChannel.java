package cn.ggsn.openrxlight.model.billing;

import com.fasterxml.jackson.databind.annotation.EnumNaming;

@EnumNaming(com.fasterxml.jackson.databind.EnumNamingStrategies.SnakeCaseStrategy.class)
public enum PaymentChannel {
    WECHAT_PAY,
    ALIPAY,
    ;

    public static PaymentChannel fromString(String paymentChannel) {
        for (PaymentChannel channel : PaymentChannel.values()) {
            if (channel.name().equalsIgnoreCase(paymentChannel)) {
                return channel;
            }
        }
        throw new IllegalArgumentException("Unsupported payment channel: " + paymentChannel);
    }
}
