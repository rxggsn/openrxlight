package cn.ggsn.openrxlight.response.orders;

import lombok.Getter;

@Getter
public enum QueueStatusCode {
    UNSUPPORTED(0, "不支持排队"),
    NOT_NEEDED(1, "有可用设备，无需排队"),
    UNAVAILABLE(2, "暂无可用设备且该区域无法排队"),
    AVAILABLE(3, "暂无可用设备且该区域可排队");

    private final int value;
    private final String description;

    QueueStatusCode(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public static QueueStatusCode fromCode(int code) {
        for (QueueStatusCode statusCode : QueueStatusCode.values()) {
            if (statusCode.getValue() == code) {
                return statusCode;
            }
        }
        return UNSUPPORTED;
    }
}
