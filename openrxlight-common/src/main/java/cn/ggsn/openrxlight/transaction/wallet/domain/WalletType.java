package cn.ggsn.openrxlight.transaction.wallet.domain;

import lombok.Getter;

public enum WalletType {
    UNSPECIFIED(0),
    CONSUMER(1);
    @Getter
    private final int value;

    WalletType(int value) {
        this.value = value;
    }

    public static WalletType fromValue(int walletType) {
        for (WalletType type : WalletType.values()) {
            if (type.getValue() == walletType) {
                return type;
            }
        }
        return UNSPECIFIED;
    }
}
