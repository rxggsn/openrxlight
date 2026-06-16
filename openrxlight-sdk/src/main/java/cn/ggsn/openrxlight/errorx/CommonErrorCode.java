package cn.ggsn.openrxlight.errorx;

public enum CommonErrorCode implements ErrorCode {
    MissingRequiredArgument(Constants.COMMON_BIZ_CODE + 1, "missing required [%s] argument"),
    TokenExpired(Constants.COMMON_BIZ_CODE + 2, "token expired"),
    NoAuthorization(Constants.COMMON_BIZ_CODE + 3, "no authorization [%s]"),
    SmsSentFailed(Constants.COMMON_BIZ_CODE + 4, "Sms sent failed: [code: %s, message: %s]"),
    CommandKeyIsRequired(Constants.COMMON_BIZ_CODE + 5, "command key [%s] is required"),
    CommandKeyIsInvalid(Constants.COMMON_BIZ_CODE + 6, "command key [%s] is invalid"),
    InvalidArgument(Constants.COMMON_BIZ_CODE + 7, "invalid argument [%s]"),
    InvalidSignature(Constants.COMMON_BIZ_CODE + 8, "invalid signature"),
    RequestTimeout(Constants.COMMON_BIZ_CODE + 9, "request timeout"),
    InvalidToken(Constants.COMMON_BIZ_CODE + 10, "invalid token"),
    MissingRequiredHeaderForFeignAuth(Constants.COMMON_BIZ_CODE + 11, "missing required header [%s] for feign auth"),
    TransDataErr(Constants.COMMON_BIZ_CODE + 12, "转化数据失败"),
    HttpRequestFailed(Constants.COMMON_BIZ_CODE + 13, "http请求失败, 状态码: %d, 响应内容: %s"),
    LocationRequired(Constants.COMMON_BIZ_CODE + 14, "location is required"),
    ;

    private final int value;
    private final String message;

    CommonErrorCode(int value, String message) {
        this.value = value;
        this.message = message;
    }

    public int getValue() {
        return value;
    }

    public String getMessage() {
        return message;
    }
}
