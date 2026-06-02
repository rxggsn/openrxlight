package cn.ggsn.openrxlight.model.order;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PayChannel {
    WECHAT((short) 1, "微信支付"),
    ALIPAY((short) 2, "支付宝"),
    UNIONPAY((short) 3, "银联支付"),
    APPLE_PAY((short) 4, "Apple Pay"),
    PAYPAL((short) 5, "PayPal"),
    CREDIT_CARD((short) 6, "信用卡支付"),
    OTHER((short) 99, "其他支付方式");

    private final short value;
    private final String description;

    public static PayChannel fromValue(short payChannel) {
        for (PayChannel channel : PayChannel.values()) {
            if (channel.value == payChannel) {
                return channel;
            }
        }
        return null;
    }
}
