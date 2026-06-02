package cn.ggsn.rxlight.account.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import cn.ggsn.openrxlight.account.domain.AccountExtInfo;
import cn.ggsn.openrxlight.account.error.AccountError;
import cn.ggsn.openrxlight.errorx.BizException;
import cn.ggsn.openrxlight.lang.Lists2;
import cn.ggsn.openrxlight.request.Required;
import cn.ggsn.openrxlight.request.Validate;
import cn.ggsn.openrxlight.transaction.domain.PaymentMethod;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonNaming(com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ConsumerAccountExtInfo implements AccountExtInfo {

    private static final int MAXIMUM_CAR_INFO_SIZE = 10;
    public String phoneNo;
    private List<CarInfo> carInfos;
    private List<PaymentMethod> supportedPaymentMethods;
    @Setter
    private UUID walletId;

    public void addCarInfos(List<CarInfo> carInfos) {
        if (Lists2.isEmpty(carInfos)) {
            return;
        }
        if (this.carInfos == null) {
            this.carInfos = Lists2.empty();
            carInfos.get(0).setDefault(true);
        }

        this.carInfos.addAll(carInfos);
    }

    public void addCarInfo(CarInfo carInfo) {
        if (Lists2.isEmpty(this.carInfos)) {
            this.carInfos = Lists2.empty();
            carInfo.setDefault(true);
        }
        if (Lists2.contains(this.carInfos, info -> StringUtils.equals(info.getPlateNo(), carInfo.getPlateNo()))) {
            throw new BizException(AccountError.DuplicatedPlateNo, carInfo.getPlateNo());
        }
        if (Lists2.size(this.carInfos) > MAXIMUM_CAR_INFO_SIZE) {
            throw new BizException(AccountError.TooManyCarInfo);
        }

        this.carInfos.add(carInfo);
        if (carInfo.isDefault()) {
            this.setDefaultCarNo(carInfo.getPlateNo());
        }
    }

    public CarInfo getDefaultCarInfo() {
        if (Lists2.isEmpty(this.carInfos)) {
            return null;
        }
        return this.carInfos.get(0);
    }

    public void setDefaultCarNo(String plateNo) {
        if (Lists2.isEmpty(this.carInfos)) {
            return;
        }
        if (StringUtils.equals(this.carInfos.get(0).getPlateNo(), plateNo)) {
            return;
        }
        this.carInfos.get(0).setDefault(false);
        Lists2.filter(this.carInfos, carInfo -> StringUtils.equals(carInfo.getPlateNo(), plateNo))
                .forEach(carInfo -> carInfo.setDefault(true));
        this.carInfos.sort(Comparator.comparing(CarInfo::isDefault).reversed());
    }

    public void removeCarInfo(String carNo) {
        if (Lists2.isEmpty(this.carInfos)) {
            return;
        }

        CarInfo defaultCarInfo = this.getDefaultCarInfo();
        if (defaultCarInfo != null &&
                StringUtils.equals(defaultCarInfo.getPlateNo(), carNo) &&
                this.carInfos.size() > 1) {
            this.setDefaultCarNo(this.carInfos.get(1).getPlateNo());
        }
        Lists2.removeIf(this.carInfos, carInfo -> StringUtils.equals(carInfo.getPlateNo(), carNo));
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @EqualsAndHashCode
    @JsonNaming(com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class CarInfo implements Validate {
        @Required
        private String plateNo;
        @Required
        private Long brandId;
        private Long categoryId; // 该字段废弃
        @Required
        private Long modelId;
        @Setter
        @JsonProperty("isDefault")
        private boolean isDefault;

        private Long subModelId; // 车型Id

        private BigDecimal batteryCapacity; // 电池容量，单位kWh

        // 为兼容所做处理
        public String genCarSubKeyId() {
            if (Objects.nonNull(this.subModelId)) {
                return this.modelId + "_" + this.subModelId;
            }
            return String.valueOf(this.modelId);
        }
    }

    public Optional<CarInfo> getCarInfo(String plateNo) {
        if (Lists2.isEmpty(this.carInfos)) {
            return Optional.empty();
        }
        return Lists2.filter(this.carInfos, carInfo -> StringUtils.equals(carInfo.getPlateNo(), plateNo))
                .stream()
                .findFirst();
    }
}
