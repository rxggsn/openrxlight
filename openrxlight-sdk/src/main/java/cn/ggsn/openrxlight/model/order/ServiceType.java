package cn.ggsn.openrxlight.model.order;

import lombok.Getter;

@Getter
public enum ServiceType {
    FastCharge(1, "快充"),
    SlowCharge(2, "慢充"),
    V2G(3, "V2G"),
    ;

    private final int value;
    private final String message;

    ServiceType(int value, String message) {
        this.value = value;
        this.message = message;
    }
}
