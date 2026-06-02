package cn.ggsn.rxlight.ai.event;

import lombok.Getter;

@Getter
public enum EventSubject {
    Message("message"),
    Heartbeat("heartbeat"),
    FormAction("form.action"),
    Notify("notify"),;

    private final String subject;

    EventSubject(String subject) {
        this.subject = subject;
    }

    public static EventSubject fromValue(String value) {
        for (EventSubject eventSubject : EventSubject.values()) {
            if (eventSubject.getSubject().equals(value)) {
                return eventSubject;
            }
        }
        return null;
    }
}
