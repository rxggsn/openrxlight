package cn.ggsn.openrxlight.transaction.wallet.domain;

import lombok.Getter;

public enum WalletStatus {
    UNSPECIFIED(0),
    ACTIVE(1),
    ;
    @Getter
    private final int value;

    WalletStatus(int value) {
        this.value = value;
    }

    public static WalletStatus fromValue(int status) {
        for (WalletStatus walletStatus : WalletStatus.values()) {
            if (walletStatus.getValue() == status) {
                return walletStatus;
            }
        }
        return UNSPECIFIED;
    }
}
