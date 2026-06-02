package cn.ggsn.openrxlight.model;

import lombok.Getter;

public enum CurrencyType {
    UNSPECIFIED(0),
    CNY(1),
    ;

    @Getter
    private final int value;

    CurrencyType(int value) {
        this.value = value;
    }

    public static CurrencyType fromValue(int value) {
        for (CurrencyType currencyType : CurrencyType.values()) {
            if (currencyType.getValue() == value) {
                return currencyType;
            }
        }
        return UNSPECIFIED;
    }
}
