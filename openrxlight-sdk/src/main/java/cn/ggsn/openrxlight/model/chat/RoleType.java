package cn.ggsn.openrxlight.model.chat;

import lombok.Getter;

@Getter
public enum RoleType {
    USER("user"),
    ASSISTANT("assistant"),
    SYSTEM("system"),
    ;

    private final String name;

    RoleType(String name) {
        this.name = name;
    }
}
