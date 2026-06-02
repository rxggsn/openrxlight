package cn.ggsn.openrxlight.event;

import lombok.Getter;

public enum ContentType {
    APPLICATION_JSON("application/json"),
    TEXT_PLAIN("text/plain"),
    BIN_PROTOBUF("binary/protobuf");

    @Getter
    private final String type;

    ContentType(String type) {
        this.type = type;
    }

    public static ContentType fromString(String type) {
        for (ContentType contentType : ContentType.values()) {
            if (contentType.getType().equalsIgnoreCase(type)) {
                return contentType;
            }
        }
        throw new IllegalArgumentException("Unsupported content type: " + type);
    }
}
