package cn.ggsn.openrxlight.request.chat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserMessageType {
    UNKNOWN("unknown", "未知"),
    TEXT("text", "文字"),
    FILE("file", "文件"),
    AUDIO("audio", "语音"),
    IMAGE("image", "图片"),
    CALLBACK("callback", "回调消息"),
    POST("post", "富文本"),
    INTERACTIVE("interactive", "交互式消息"),
    LOCATION("location", "位置消息"),;

    private final String name;
    private final String desc;

    public static UserMessageType fromName(String name) {
        for (UserMessageType type : UserMessageType.values()) {
            if (type.getName().equals(name)) {
                return type;
            }
        }
        return null;
    }
}
