package cn.ggsn.rxlight.orders.request;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import cn.ggsn.openrxlight.model.billing.PaymentChannel;
import cn.ggsn.openrxlight.model.order.ChargeBasicType;
import cn.ggsn.openrxlight.model.order.OrderType;
import cn.ggsn.openrxlight.model.order.PayType;
import cn.ggsn.openrxlight.request.Required;
import cn.ggsn.openrxlight.request.Validate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CreateOrderRequest implements Validate {
    @Required
    private PaymentChannel paymentChannel;
    @Required
    private String plateNo;
    @Required
    private OrderType orderType;
    @Required
    private Long stationId;
    @Required
    private Long spaceId;
    @Required
    private ChargeBasicType chargeBasicType;
    @Required
    private String chargeBasicValue;
    @Required
    private PayType payType;
    private Integer prepay;

    @Override
    public void validate() {
        Validate.super.validate();

        if (PayType.PRE_FEE.equals(this.payType)) {
            if (this.prepay == null || this.prepay == 0) {
                throw new IllegalArgumentException("field pay_type is required");
            }
        }
    }

}
