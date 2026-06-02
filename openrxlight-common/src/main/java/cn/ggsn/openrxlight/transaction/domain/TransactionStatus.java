package cn.ggsn.openrxlight.transaction.domain;

import com.wechat.pay.java.service.payments.model.Transaction;

import cn.ggsn.openrxlight.transaction.wallet.domain.WalletTxnStatus;
import lombok.Getter;

public enum TransactionStatus {
    UNSPECIFIED(0),
    INITIALIZED(1),
    SUCCESS(2),
    REFUND(3),
    NOT_PAYED(4),
    CLOSED(5),
    REVOKED(6),
    PAYING(7),
    FAILURE(8),
    REFUNDING(9),
    PARTIALLY_REFUND(10),
    REFUND_FAILURE(11),
    REFUND_CLOSED(12),
    ;

    @Getter
    private final int value;

    TransactionStatus(int value) {
        this.value = value;
    }

    public static TransactionStatus fromValue(int value) {
        for (TransactionStatus transactionStatus : TransactionStatus.values()) {
            if (transactionStatus.getValue() == value) {
                return transactionStatus;
            }
        }
        return INITIALIZED;
    }

    public static TransactionStatus fromWechatTradeState(Transaction.TradeStateEnum state) {
        switch (state) {
            case SUCCESS:
                return TransactionStatus.SUCCESS;
            case REFUND:
                return TransactionStatus.REFUND;
            case NOTPAY:
                return TransactionStatus.NOT_PAYED;
            case CLOSED:
                return TransactionStatus.CLOSED;
            case REVOKED:
                return TransactionStatus.REVOKED;
            case USERPAYING:
                return TransactionStatus.PAYING;
            case PAYERROR:
                return TransactionStatus.FAILURE;
            default:
                return TransactionStatus.INITIALIZED;
        }
    }

    public static TransactionStatus fromWalletStatus(WalletTxnStatus status) {
        switch (status) {
            case SUCCESS:
                return TransactionStatus.SUCCESS;
            case FAILURE:
                return TransactionStatus.FAILURE;
            default:
                return TransactionStatus.UNSPECIFIED;
        }
    }

    public boolean isComplete() {
        return TransactionStatus.CLOSED.equals(this) ||
                TransactionStatus.FAILURE.equals(this) ||
                TransactionStatus.SUCCESS.equals(this) ||
                TransactionStatus.REFUND.equals(this) ||
                TransactionStatus.REVOKED.equals(this) ||
                TransactionStatus.PARTIALLY_REFUND.equals(this);
    }

    public boolean isRefundSuccess() {
        return TransactionStatus.REFUND.equals(this) ||
                TransactionStatus.PARTIALLY_REFUND.equals(this);
    }

    public boolean isRefundFailure() {
        return TransactionStatus.REFUND_FAILURE.equals(this);
    }
}
