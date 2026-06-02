package cn.ggsn.openrxlight.transaction.domain;

import lombok.Getter;

public enum PaymentMethod {
    UNSPECIFIED(0),
    WALLET(1),
    PRE_PAY(2),
    PWD_FREE(3),
    CHARGE_CARD(4),
    ;

    @Getter
    private final int value;

    PaymentMethod(int value) {
        this.value = value;
    }
}
