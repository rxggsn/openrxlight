package cn.ggsn.rxlight.orders.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import cn.ggsn.openrxlight.domain.BaseEntity;
import cn.ggsn.openrxlight.model.Currency;
import cn.ggsn.openrxlight.model.order.OpsOrder.OrderStatus;
import cn.ggsn.openrxlight.model.order.OrderType;
import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "order")
public class RxLightOrder extends BaseEntity {

    @Getter
    @RequiredArgsConstructor
    public enum PayType {
        UNSPECIFIED((short) 0, "未知"),
        WALLET((short) 1, "余额支付"),
        PWD_FREE((short) 4, "微信分免密支付"),

        PRE_FEE((short) 5, "预支付"),
        CHARGING_CARD((short) 7, "充电卡支付"), // 针对度数充电卡使用
        ;

        private final short value;

        private final String message;

        public static PayType fromValue(int payType) {
            for (PayType type : PayType.values()) {
                if (type.value == payType) {
                    return type;
                }
            }
            return UNSPECIFIED;
        }

        // 主要用于判断结束订单的方式
        public static PayType fromValue(short payType, Integer subPayType) {
            for (PayType type : PayType.values()) {
                if (Objects.nonNull(subPayType)) {
                    if (subPayType == UNSPECIFIED.value) {
                        return UNSPECIFIED;
                    }

                    return fromValue(subPayType);
                } else if (type.value == payType) {
                    return type;
                }
            }
            return UNSPECIFIED;
        }
    }

    @Getter
    @RequiredArgsConstructor
    public enum AdditionalFeeTypeEnum {
        UNSPECIFIC("未知", (short) 0),
        GUN_ACTION_SERVICE("代插拔枪服务费", (short) 1),
        OVERTIME("超时费", (short) 2),
        QUEUE_SERVICE("排队服务费", (short) 3);

        private final String desc;
        private final short value;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @EqualsAndHashCode(of = "feeType")
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class AdditionalFee {
        private int value; // 附加费用金额, 单位分
        private int feeType; // 附加费用类型

        public AdditionalFeeTypeEnum checkFeeType() {
            for (AdditionalFeeTypeEnum feeTypeEnum : AdditionalFeeTypeEnum.values()) {
                if (feeTypeEnum.getValue() == this.feeType) {
                    return feeTypeEnum;
                }
            }
            return AdditionalFeeTypeEnum.UNSPECIFIC;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class AdditionalInfo {
        private Integer stopReason;
        private List<Long> deviceIds;
    }

    @Getter
    @AllArgsConstructor
    public enum SourceType {
        OPEN_RXLIGHT((short) 1, "Open RxLight Platform"),;

        private final short value;
        private final String description;
    }

    @Column(name = "order_no", nullable = false)
    private String orderNo;

    @Column(name = "third_party_order_no", nullable = false)
    private String thirdPartyOrderNo;

    @Column(name = "order_type", nullable = false)
    private Short orderType;

    @Column(name = "status", nullable = false)
    private Short status;

    @Column(name = "station_id", nullable = false)
    private Long stationId;

    @Column(name = "station_name", nullable = false)
    private String stationName;

    @Column(name = "space_id", nullable = false)
    private Long spaceId;

    @Column(name = "range_id", nullable = false)
    private Long rangeId;

    @Column(name = "space_no", nullable = false)
    private String spaceNo;

    @Column(name = "account_id", nullable = true)
    private UUID accountId;

    @Column(name = "plate_no", nullable = true)
    private String plateNo;

    @Column(name = "phone_no", nullable = true)
    private String phoneNo;

    @Column(name = "charge_start_time", nullable = true)
    private LocalDateTime chargeStartTime;

    @Column(name = "charge_end_time", nullable = true)
    private LocalDateTime chargeEndTime;

    @Column(name = "charged_power", nullable = true)
    private Integer chargedPower;

    @Column(name = "service_ccy", nullable = true)
    private Integer serviceCcy;

    @Column(name = "charge_ccy", nullable = true)
    private Integer chargeCcy;

    @Column(name = "total_ccy", nullable = true)
    private Integer totalCcy;

    @Column(name = "start_soc", nullable = true)
    private Integer startSoc;

    @Column(name = "end_soc", nullable = true)
    private Integer endSoc;

    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;

    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime;

    @Column(name = "node_id", nullable = false)
    private Long merchantId;

    @Column(name = "additional_fees", nullable = true)
    @JdbcType(org.hibernate.dialect.type.PostgreSQLJsonPGObjectJsonbType.class)
    @JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    private List<AdditionalFee> additionalFees;

    @Column(name = "source_type", nullable = false)
    private Short sourceType;

    @Column(name = "transaction_id", nullable = true)
    private UUID transactionId;

    @Column(name = "pay_type", nullable = false)
    private Short payType;
    @Column(name = "car_model_id", nullable = false)
    private String carModelId;
    @Column(name = "pre_fee", nullable = true)
    private Integer preFee; // prepay fee, unit cent
    @Column(name = "ccy_type", nullable = false)
    private String currencyType; // ISO 4217 Currency Code
    @Column(name = "payment_channel", nullable = false)
    private Short paymentChannel;
    @Column(name = "additional_info", nullable = false)
    @JdbcType(org.hibernate.dialect.type.PostgreSQLJsonPGObjectJsonbType.class)
    @JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    private AdditionalInfo additionalInfo;

    public static Optional<RxLightOrder> getByThirdPartyOrderNo(String orderNo) {
        return QuarkusTransaction.joiningExisting().call(() -> {
            CriteriaBuilder cb = RxLightOrder.getEntityManager().getCriteriaBuilder();
            var query = cb.createQuery(RxLightOrder.class);
            var root = query.from(RxLightOrder.class);
            Optional<RxLightOrder> result = RxLightOrder.getEntityManager()
                    .createQuery(query.select(root).where(cb.equal(root.get("third_party_order_no"), orderNo)))
                    .getResultStream().findFirst();
            return result;
        });
    }

    @JsonIgnore
    public Currency getPrePayFee() {
        return Currency.ofCent(this.currencyType, this.preFee);
    }

    @JsonIgnore
    public String getDescription(String i18n) {
        switch (OrderType.fromValue(this.orderType)) {
            case V2V:
                switch (i18n) {
                    case "zh":
                        return "黑马原力充电预付费";
                    default:
                        return "DHForce AI Prepaid Order";
                }
            default:
                return null;
        }

    }

    public static Optional<RxLightOrder> getByOrderNo(String orderNo) {
        return QuarkusTransaction.joiningExisting().call(() -> {
            CriteriaBuilder cb = RxLightOrder.getEntityManager().getCriteriaBuilder();
            var query = cb.createQuery(RxLightOrder.class);
            var root = query.from(RxLightOrder.class);
            Optional<RxLightOrder> result = RxLightOrder.getEntityManager()
                    .createQuery(query.select(root).where(cb.equal(root.get("torder_no"), orderNo)))
                    .getResultStream().findFirst();
            return result;
        });
    }

    public void updateStatus(OrderStatus status) {
        QuarkusTransaction.joiningExisting().run(() -> {
            CriteriaBuilder cb = RxLightOrder.getEntityManager().getCriteriaBuilder();
            var update = cb.createCriteriaUpdate(RxLightOrder.class);
            var root = update.from(RxLightOrder.class);
            this.status = status.getValue();
            if (status.isFinished()) {
                this.chargeEndTime = LocalDateTime.now();
                update.set("status", status.getValue())
                        .set("chargeEndTime", LocalDateTime.now())
                        .where(cb.equal(root.get("orderNo"), this.orderNo));
            } else {
                update.set("status", status.getValue())
                        .where(cb.equal(root.get("orderNo"), this.orderNo));
            }

            RxLightOrder.getEntityManager().createQuery(update).executeUpdate();
        });
    }
}
