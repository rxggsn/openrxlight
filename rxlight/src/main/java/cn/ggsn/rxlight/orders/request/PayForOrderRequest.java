package cn.ggsn.rxlight.orders.request;

import cn.ggsn.openrxlight.request.Required;
import cn.ggsn.openrxlight.request.Validate;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PayForOrderRequest implements Validate {
    @Required
    private String orderNo;
}
