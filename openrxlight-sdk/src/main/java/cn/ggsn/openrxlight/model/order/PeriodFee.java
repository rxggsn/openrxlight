package cn.ggsn.openrxlight.model.order;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PeriodFee {
    private String startTime;
    private String endTime;
    private Integer chargeCcy;
    private Integer serviceCcy;
    private Integer chargePower;
    private Integer chargeUnitFee;
    private Integer serviceUnitFee;
}