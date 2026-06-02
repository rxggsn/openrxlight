package cn.ggsn.openrxlight.transaction.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum TransactionType {
    UNSPECIFIED((short) 0),
    WECHAT_PAY((short) 1),
    WALLET_PAY((short) 3),
    WALLET_DEPOSIT((short) 4),
    WALLET_WITHDRAW((short) 5),
    WECHAT_REFUND((short) 6),
    ;

    @Getter
    private final short value;

    public static TransactionType fromValue(int value) {
        for (TransactionType transactionType : TransactionType.values()) {
            if (transactionType.getValue() == (short) value) {
                return transactionType;
            }
        }
        return UNSPECIFIED;
    }
}
