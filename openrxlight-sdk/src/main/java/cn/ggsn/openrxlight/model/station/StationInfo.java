package cn.ggsn.openrxlight.model.station;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StationInfo {
    private Long id;
    private String name;
    private String address;
    private String serviceTel;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer status;
    private List<ChargeFeeDetail> fastChargeFeeDetails;
    private List<ChargeFeeDetail> slowChargeFeeDetails;
    private List<ChargeFeeDetail> superChargeFeeDetails;
    private List<String> logos;
    private String cityPath;
    private List<OperationType> operationTypes;

    // Inner class for charge fee details
    @Getter
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChargeFeeDetail {
        private String startTime;
        private String endTime;
        private BigDecimal chargeFee;
        private BigDecimal serviceFee;
    }

    // 站点状态，0-未知，1-待运营，2-运营中，3-暂停运营，4-已关闭
    public enum StationStatus {
        STATUS_UNSPECIFIED(0, "未知"),
        STATUS_WAIT_OPERATION(1, "待运营"),
        STATUS_OPERATING(2, "运营中"),
        STATUS_STOP_OPERATION(3, "暂停运营"),
        STATUS_CLOSE(4, "关闭"),
        ;

        @Getter
        private final int value;

        @Getter
        private final String message;

        StationStatus(int value, String message) {
            this.value = value;
            this.message = message;
        }

        public static StationStatus fromValue(int value) {
            for (StationStatus status : StationStatus.values()) {
                if (status.getValue() == value) {
                    return status;
                }
            }
            return null;
        }
    }
}
