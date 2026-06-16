package cn.ggsn.openrxlight.model.chat.callback.event;

import java.math.BigDecimal;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import cn.ggsn.openrxlight.model.order.ChargeBasicType;
import cn.ggsn.openrxlight.model.order.OrderType;
import cn.ggsn.openrxlight.model.station.StationInfo;
import cn.ggsn.openrxlight.model.station.StationSpace;
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
@JsonNaming(SnakeCaseStrategy.class)
public class ConfirmOrder implements Validate {
    public static final String CALLBACK_TYPE = "confirm_order";

    @Required
    private String plateNo;
    @Required
    private OrderType orderType;
    @Required
    private StationInfo station;
    @Required
    private StationSpace space;
    @Required
    private ChargeBasicType chargeBasicType;
    @Required
    private BigDecimal chargeBasicValue;
    @Required
    private BigDecimal prepay;

    public String getChargeBasicDesc() {
        switch (this.chargeBasicType) {
            case SOC:
                if (this.chargeBasicValue == BigDecimal.valueOf(100)) {
                    return "充满";
                } else {
                    return String.format("按 SOC 充：充到%d%", this.chargeBasicValue);
                }
            case FEE:
                return String.format("按金额充：充%d元", this.chargeBasicValue);
            default:
                return "";
        }
    }
}
