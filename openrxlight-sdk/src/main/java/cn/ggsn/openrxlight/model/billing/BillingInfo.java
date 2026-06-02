package cn.ggsn.openrxlight.model.billing;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy.class)
@Builder
public class BillingInfo {
    private Integer usedCredit;
    private Integer totalCredit;
    private String packageName;
    private String packageDescription;
    private LocalDateTime validUntil;
    private BillingCycle billingCycle;
    private Integer extraCredit;

    @JsonIgnore
    public Integer getRemainingCredit() {
        return totalCredit + extraCredit - usedCredit;
    }
}
