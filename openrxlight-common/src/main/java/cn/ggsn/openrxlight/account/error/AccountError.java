package cn.ggsn.openrxlight.account.error;

import cn.ggsn.openrxlight.errorx.Constants;
import cn.ggsn.openrxlight.errorx.ErrorCode;
import lombok.Getter;

@Getter
public enum AccountError implements ErrorCode {
    VerificationCodeExpired(Constants.CMS_BIZ_CODE + 1, "Verification code expired"),
    VerificationCodeIncorrect(Constants.CMS_BIZ_CODE + 2, "Verification code incorrect"),
    AccountNotExist(Constants.CMS_BIZ_CODE + 3, "Account [%s] does not exist"),
    NotSupportExternalAccountType(Constants.CMS_BIZ_CODE + 4, "Not support external account type [%s]"),
    InvalidAuthorizationInfo(Constants.CMS_BIZ_CODE + 5, "Invalid authorization info"),
    AccountIsNotActive(Constants.CMS_BIZ_CODE + 6, "Account [%s] is not active"),
    DuplicatedPlateNo(Constants.CMS_BIZ_CODE + 7, "Duplicated plate no [%s]"),
    WechatApiNotInitialized(Constants.CMS_BIZ_CODE + 8, "Wechat api not initialized"),
    ExternalAccountNotFound(Constants.CMS_BIZ_CODE + 9, "External configuration not found"),
    TooManyCarInfo(Constants.CMS_BIZ_CODE + 10, "Too many car info"),
    SecretInvalid(Constants.CMS_BIZ_CODE + 11, "Secret invalid"),
    PermissionDenied(Constants.CMS_BIZ_CODE + 12, "Permission denied: %s"),
    PasswordIncorrect(Constants.CMS_BIZ_CODE + 13, "Password incorrect"),
    NotFoundPlateNo(Constants.CMS_BIZ_CODE + 14, "plate no [%s] not found"),
    ;

    private final int value;
    private final String message;

    AccountError(int value, String message) {
        this.value = value;
        this.message = message;
    }
}