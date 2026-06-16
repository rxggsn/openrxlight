package cn.ggsn.rxlight.ai.agent.impl.lark.vo;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import cn.ggsn.openrxlight.model.billing.PaymentChannel;
import cn.ggsn.openrxlight.model.order.ChargeBasicType;
import cn.ggsn.openrxlight.model.order.OrderType;
import cn.ggsn.openrxlight.request.Required;
import cn.ggsn.openrxlight.request.Validate;
import lombok.Getter;

@Getter
@JsonNaming(SnakeCaseStrategy.class)
public class ConfirmOrderPayment implements Validate {
    @Required
    private PaymentChannel paymentChannel;
    @Required
    private String plateNo;
    @Required
    private OrderType orderType;
    @Required
    private String station;
    @Required
    private String space;
    @Required
    private ChargeBasicType chargeBasicType;
    @Required
    private String chargeBasicValue;
    @Required
    private String prepay;
    @Required
    private String initialSoc;

    @JsonIgnore
    public BigDecimal getPrepayCcy() {
        return new BigDecimal(this.prepay);
    }
}
