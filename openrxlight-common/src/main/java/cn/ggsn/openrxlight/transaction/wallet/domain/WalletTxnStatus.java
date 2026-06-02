package cn.ggsn.openrxlight.transaction.wallet.domain;

import lombok.Getter;

public enum WalletTxnStatus {
    UNSPECIFIED(0),
    SUCCESS(1),
    FAILURE(2);
    @Getter
    private final int value;

    WalletTxnStatus(int value) {
        this.value = value;
    }
}
