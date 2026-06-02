package cn.ggsn.openrxlight.transaction.error;

import cn.ggsn.openrxlight.errorx.ErrorCode;
import cn.ggsn.openrxlight.errorx.Constants;

public enum TpsErrorCode implements ErrorCode {
    TransactionNotFound(Constants.TXN_BIZ_CODE + 1, "transaction [%s] not found"),
    InsufficientBalance(Constants.TXN_BIZ_CODE + 2, "insufficient balance"),
    RefundCcyAmountExceed(Constants.TXN_BIZ_CODE + 3, "refund ccy amount exceed"),
    TooManyRefunds(Constants.TXN_BIZ_CODE + 4, "too many refunds"),
    UnsupportedPaymentChannel(Constants.TXN_BIZ_CODE + 5, "unsupported payment channel [%s]"),
    WebNotSupported(Constants.TXN_BIZ_CODE + 6, "initiate a transaction from web does not supported"),
    NotSupportDepositChannel(Constants.TXN_BIZ_CODE + 7, "not supported deposit channel [%s]"),
    InProcessTransactionCannotRefund(Constants.TXN_BIZ_CODE + 8, "in process transaction cannot refund"),
    AlreadyRefunded(Constants.TXN_BIZ_CODE + 9, "already refunded"),
    UnsupportedRefundTransaction(Constants.TXN_BIZ_CODE + 10, "unsupported refund transaction [%s]"),
    WithdrawalInProgress(Constants.TXN_BIZ_CODE + 11, "there is withdraw in progress"),
    CANNOT_WITHDRAW_WHEN_ONGOING(Constants.TXN_BIZ_CODE + 12, "cannot withdraw when you have ongoing order"),
    CounterpartyAccountNotFound(Constants.TXN_BIZ_CODE + 13, "counterparty account [%s] not found"),
    ;

    private final int value;
    private final String message;

    TpsErrorCode(int value, String message) {
        this.value = value;
        this.message = message;
    }

    @Override
    public int getValue() {
        return this.value;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
