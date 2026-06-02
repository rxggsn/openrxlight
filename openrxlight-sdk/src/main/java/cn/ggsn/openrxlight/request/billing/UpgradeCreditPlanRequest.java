package cn.ggsn.openrxlight.request.billing;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import cn.ggsn.openrxlight.model.billing.PaymentChannel;
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
public class UpgradeCreditPlanRequest implements Validate {
    @Required
    private Integer packageId;
    @Required
    private PaymentChannel paymentChannel;
    @Required
    private String currencyType;
}
