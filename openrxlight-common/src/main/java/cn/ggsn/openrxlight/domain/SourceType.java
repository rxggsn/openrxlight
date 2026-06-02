package cn.ggsn.openrxlight.domain;

import lombok.Getter;

public enum SourceType {
    UNSPECIFIED(0),
    WEAPP(1),
    WEB(2),
    PARTNER(3),
    CLI(4),
    APP(5);

    @Getter
    private final int value;

    SourceType(int value) {
        this.value = value;
    }

    public static SourceType fromValue(int sourceType) {
        for (SourceType type : SourceType.values()) {
            if (type.getValue() == sourceType) {
                return type;
            }
        }
        return UNSPECIFIED;
    }
}
