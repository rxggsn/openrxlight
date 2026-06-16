package cn.ggsn.openrxlight.model.order;

import java.util.Objects;

import com.fasterxml.jackson.databind.EnumNamingStrategies;
import com.fasterxml.jackson.databind.annotation.EnumNaming;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@EnumNaming(EnumNamingStrategies.LowerCaseStrategy.class)
public enum ChargeBasicType {
    UNSPECIFIED(0, "未知"),
    SOC(1, "按充电比率"),
    FEE(2, "按金额充电"),
    ;

    private final int value;

    private final String message;

    public static ChargeBasicType fromValue(Integer chargedBasicType) {
        if (Objects.isNull(chargedBasicType)) {
            return UNSPECIFIED;
        }
        for (ChargeBasicType type : ChargeBasicType.values()) {
            if (type.value == chargedBasicType) {
                return type;
            }
        }
        return UNSPECIFIED;
    }
}
