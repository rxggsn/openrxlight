package cn.ggsn.openrxlight.event.order;

import java.time.LocalDateTime;
import java.util.List;

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
public class OrderInfo {
    private String merchantOrderNo;
    private String phoneNo;
    private String plateNo;
    private Long stationId;
    private String spaceNo;
    private Short status;
    private String statusDesc;
    private LocalDateTime chargeStartTime;
    private String orderNo;
    private LocalDateTime chargeEndTime;
    private Integer chargeCcy;
    private Integer serviceCcy;
    private Integer totalCcy;
    private Integer chargePower;
    private Long deviceId;
    private List<PeriodFee> periodFees;
    private Integer stopReason;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class PeriodFee {
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Integer chargeCcy;
        private Integer serviceCcy;
        private Integer chargePower;
        private Integer chargeUnitCcy;
        private Integer serviceUnitCcy;
    }
}
