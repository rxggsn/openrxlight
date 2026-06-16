package cn.ggsn.openrxlight.model.station;

import lombok.Getter;

@Getter
public enum OperationType {
    NORMAL((short) 0, "Normal Service"),
    QUEUE((short) 1, "Queue Service"),
    RESERVATION((short) 2, "Reservation Charge Service"),
    REPLAY((short) 3, "Replay Charge Service"),
    MANUAL((short) 4, "Manual Operation Service"),
    ;

    private final short value;
    private final String description;

    OperationType(short value, String description) {
        this.value = value;
        this.description = description;
    }

    public static OperationType fromValue(short value) {
        for (OperationType type : OperationType.values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        return NORMAL;
    }
}
