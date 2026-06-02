package cn.ggsn.openrxlight.domain;

import com.fasterxml.jackson.databind.EnumNamingStrategies;
import com.fasterxml.jackson.databind.annotation.EnumNaming;

import lombok.Getter;

@Getter
@EnumNaming(EnumNamingStrategies.SnakeCaseStrategy.class)
public enum ExternalAccountType {
    ANONYMOUS(0),
    WECHAT_MINI_PROGRAM(1),
    DEVELOPER(2),
    FEISHU(3),
    EMAIL(4),
    PHONE(5);

    private final int value;

    ExternalAccountType(int value) {
        this.value = value;
    }

    public static ExternalAccountType fromValue(int value) {
        for (ExternalAccountType type : ExternalAccountType.values()) {
            if (type.value == value) {
                return type;
            }
        }
        return ExternalAccountType.ANONYMOUS;
    }
}
