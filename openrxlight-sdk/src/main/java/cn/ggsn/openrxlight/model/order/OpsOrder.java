package cn.ggsn.openrxlight.model.order;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import cn.ggsn.openrxlight.model.station.QueueInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class OpsOrder {
    private String orderNo;
    private Integer status;
    private Integer serviceCcy;
    private Integer chargeCcy;
    private Integer totalCcy;
    private Integer chargePower;
    private Long stationId;
    private String spaceNo;
    private String phoneNo;
    private String plateNo;
    private QueueInfo queueInfo;
    private Long deviceId;
    private String chargeStartTime;
    private String chargeEndTime;
    private Long carTypeId;
    private List<PeriodFee> periodFees;
    private Integer stopReason;
    private Integer soc;
    private List<Long> deviceIds;

    // 订单状态
    public enum OrderStatus {
        // 订单状态，0-创建， 1-排队中，2-等待中, 3-充电中, 4-待支付, 5-已完成, 5-已取消, 7-订单异常
        // 订单状态，0-创建，1-预支付，2-等待调度，3-排队中，4-等待设备就绪, 5-充电中, 6-等待设备结束, 7-待支付, 49-已取消,
        // 50-已完成, 51-订单异常
        UNSPECIFIED(-1, "未知"),
        CREATED(0, "创建"),
        RE_FEE(1, "预支付"),
        WAITING_DISPATCH(2, "等待调度"),
        LINE_UP(3, "排队中"),
        WAITING_READY(4, "等待设备就绪"),
        CHARGING(5, "充电中"),
        WAITING_FINISH(6, "等待设备结束"),
        WAITING_PAYMENT(7, "待支付"),

        CANCEL(49, "已取消"),
        FINISH(50, "已完成"),
        EXCEPTION(51, "订单异常"),
        PENDING_COMPLETE(8, "等待结束中"),
        RESERVED(9, "预约中"),
        ;

        @Getter
        private final short value;

        @Getter
        private final String message;

        OrderStatus(int value, String message) {
            this.value = (short) value;
            this.message = message;
        }

        public static OrderStatus fromValue(Short status) {
            if (Objects.isNull(status)) {
                return UNSPECIFIED;
            }
            for (OrderStatus orderStatus : OrderStatus.values()) {
                if (orderStatus.value == status) {
                    return orderStatus;
                }
            }
            return UNSPECIFIED;
        }

        public boolean isSuccess() {
            return this == FINISH;
        }

        public boolean isFinished() {
            return this == FINISH || this == CANCEL || this == EXCEPTION;
        }
    }

    // 订单终止原因，0-未知，1-手动终止，2-设备端终止，3-符合条件终止，4-金额不足终止
    public enum StopReason {
        STOP_NO_DATA(-1, ""),
        STOP_UNSPECIFIED(0, "未知"),
        STOP_MAN_MADE(1, "用户C端手动终止"),
        STOP_DEVICE(2, "设备端终止"),
        STOP_MEET_EXPECTATIONS(3, "符合用户设定（按金额/按soc）终止"),
        STOP_BALANCE(4, "金额不足终止"),
        STOP_NO_SIGNAL(5, "无信号终止"),
        STOP_NO_DISPATCH_EXP(6, "调度异常终止"),
        STOP_DISPATCH_CANCEL(7, "调度取消"),
        STOP_NO_PLATFORM(8, "平台端终止"),
        STOP_NO_DISPATCH(9, "调度终止"),
        STOP_NO_CHARGING_CARD(10, "充电卡余额不足终止"),
        ;

        @Getter
        private final int value;

        @Getter
        private final String message;

        StopReason(int value, String message) {
            this.value = value;
            this.message = message;
        }

        public static StopReason fromValue(Integer status) {
            if (Objects.isNull(status)) {
                return STOP_NO_DATA;
            }

            for (StopReason stopReason : StopReason.values()) {
                if (stopReason.value == status) {
                    return stopReason;
                }
            }
            return STOP_UNSPECIFIED;
        }

    }
}
