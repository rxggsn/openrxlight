package cn.ggsn.rxlight.ai.domain;

import lombok.Getter;

@Getter
public enum AppType {
    FEISHU("飞书应用", 1),
    APP("APP应用", 2);

    private final String description;
    private final int code;

    AppType(String description, int code) {
        this.description = description;
        this.code = code;
    }

    public static AppType fromValue(Integer appType) {
        for (AppType type : AppType.values()) {
            if (type.getCode() == appType) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unsupported app type: " + appType);
    }
}
