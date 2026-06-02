package cn.ggsn.openrxlight.transaction.pay.wechat.error;

import cn.ggsn.openrxlight.errorx.Constants;
import cn.ggsn.openrxlight.errorx.ErrorCode;

public enum WechatPayErrorCode implements ErrorCode {
    MissingDescription(Constants.WECHAT_PAY_BIZ_CODE + 1, "missing description"),
    WechatPaySignatureEmpty(Constants.WECHAT_PAY_BIZ_CODE + 2, "signature is blank"),
    UnsupportedChannelType(Constants.WECHAT_PAY_BIZ_CODE + 3, "unsupported channel type %s"),
    CreateCreditOrderError(Constants.WECHAT_PAY_BIZ_CODE + 4, "create pay score order error %s"),
    CompleteCreditOrderError(Constants.WECHAT_PAY_BIZ_CODE + 5, "complete pay score order error %s"),
    MissingWechatMiniProgramAccount(Constants.WECHAT_PAY_BIZ_CODE + 6, "missing wechat mini program account for [%s]"),
    UnsupportedTransactionType(Constants.WECHAT_PAY_BIZ_CODE + 7, "unsupported transaction type %s"),
    ;

    private final int value;
    private final String message;

    WechatPayErrorCode(int value, String message) {
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
