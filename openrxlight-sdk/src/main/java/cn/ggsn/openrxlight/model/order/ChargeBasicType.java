package cn.ggsn.openrxlight.model.order;

import java.util.Objects;

import lombok.Getter;

public enum ChargeBasicType {
    BASIC_UNSPECIFIED(0, "未知"),
    BASIC_SOC(1, "按充电比率"),
    BASIC_FEE(2, "按金额充电"),
    BASIC_TIME(3, "按时间充电"),
    ;

    @Getter
    private final int value;

    @Getter
    private final String message;

    ChargeBasicType(int value, String message) {
        this.value = value;
        this.message = message;
    }

    public static ChargeBasicType fromValue(Integer chargedBasicType) {
        if (Objects.isNull(chargedBasicType)) {
            return BASIC_UNSPECIFIED;
        }
        for (ChargeBasicType type : ChargeBasicType.values()) {
            if (type.value == chargedBasicType) {
                return type;
            }
        }
        return BASIC_UNSPECIFIED;
    }
}
