package cn.ggsn.openrxlight.domain;

public enum AccountType {
    ANONYMOUS(0),
    CONSUMER(1),
    OPERATOR(2),
    MERCHANT(3),;

    private final int value;

    AccountType(int value) {
        this.value = value;
    }

    public static AccountType fromValue(int value) {
        for (AccountType accountType : AccountType.values()) {
            if (accountType.getValue() == value) {
                return accountType;
            }
        }
        return null;
    }

    public int getValue() {
        return value;
    }
}
