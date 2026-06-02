package cn.ggsn.rxlight.orders.request;

import cn.ggsn.openrxlight.request.Required;
import cn.ggsn.openrxlight.request.Validate;
import lombok.Getter;

@Getter
public class PayForOrderRequest implements Validate {
    @Required
    private Long orderId;
    @Required
    private Short transactionType;
}
