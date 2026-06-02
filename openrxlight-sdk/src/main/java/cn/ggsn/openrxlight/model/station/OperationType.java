package cn.ggsn.openrxlight.model.station;

import lombok.Getter;

@Getter
public enum OperationType {
    NORMAL(0, "Normal Service"),
    QUEUE(1, "Queue Service"),
    RESERVATION(2, "Reservation Charge Service"),
    REPLAY(3, "Replay Charge Service"),
    MANUAL(4, "Manual Operation Service"),
    ;

    private final int value;
    private final String description;

    OperationType(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public static OperationType fromValue(int value) {
        for (OperationType type : OperationType.values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        return NORMAL;
    }
}
