package cn.ggsn.openrxlight.web;

import lombok.Getter;

public enum RoleType {
    ANONYMOUS(0),
    GUEST(1),
    OPERATOR(2),
    EXECUTOR(3),
    ADMIN(99),
    ;

    @Getter
    private final int value;

    RoleType(int value) {
        this.value = value;
    }

    public static RoleType fromValue(int roleType) {
        for (RoleType type : RoleType.values()) {
            if (type.getValue() == roleType) {
                return type;
            }
        }
        return null;
    }

}
