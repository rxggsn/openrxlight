package cn.ggsn.openrxlight.model.billing;

import java.math.BigDecimal;

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
public class CreditPlan {
    private Integer id;
    private String name;
    private String description;
    private Integer totalCredit;
    private BigDecimal salePrice;
    private String currencyType;
    private BillingCycle billingCycle;
}
