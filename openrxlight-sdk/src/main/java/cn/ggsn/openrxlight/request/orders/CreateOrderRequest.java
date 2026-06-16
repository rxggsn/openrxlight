package cn.ggsn.openrxlight.request.orders;

import java.math.BigDecimal;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import cn.ggsn.openrxlight.model.order.OrderType;
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
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CreateOrderRequest implements Validate {
    @Required
    private String merchantOrderNo;
    @Required
    private String phoneNo;
    @Required
    private String plateNo;
    @Required
    private Long stationId;
    @Required
    private String spaceNo;
    @Required
    private Long spaceId;
    @Required
    private Integer chargeBasicType;
    @Required
    private Integer chargeBasicValue;
    @Required
    private Long carModelId;
    @Required
    private OrderType orderType;
    @Required
    private BigDecimal longitude;
    @Required
    private BigDecimal latitude;
}
