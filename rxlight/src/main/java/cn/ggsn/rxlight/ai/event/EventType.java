package cn.ggsn.rxlight.ai.event;

import lombok.Getter;

@Getter
public enum EventType {
    HeartbeatPing("heartbeat.ping"),
    HeartbeatPong("heartbeat.pong"),
    MessageRespond("message.respond"),
    FormActionTriggered("form.action.trigger");

    private final String type;

    EventType(String type) {
        this.type = type;
    }

    public static EventType fromValue(String value) {
        for (EventType eventType : EventType.values()) {
            if (eventType.getType().equals(value)) {
                return eventType;
            }
        }
        return null;
    }
}
