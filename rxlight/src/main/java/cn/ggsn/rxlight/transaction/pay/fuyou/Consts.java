package cn.ggsn.rxlight.transaction.pay.fuyou;

import java.util.regex.Pattern;

public class Consts {
    public static final String TERM_ID = "88888888";
    public static final String PREPAY_URL = "wxPreCreate";
    public static final String PRE_CREATE = "preCreate";
    public static final String REFUND_URL = "commonRefund";
    public static final String QUERY_ORDER_URL = "commonQuery";
    public static final String CREATE_CREDIT_ORDER_URL = "payscore/orderCreate";
    public static final String QUERY_CREDIT_ORDER_URL = "payscore/orderQuery";
    public static final String COMPLETE_CREDIT_ORDER_URL = "payscore/orderComplete";
    public static final String DEDUCTION_CREDIT_ORDER_URL = "payscore/deduction";
    public static final String CANCEL_CREDIT_ORDER_URL = "payscore/orderCancel";
    public static final String CLOSE_CREDIT_ORDER_URL = "closeorder";
    public static final String CHARSET = "GBK";
    public static final String SUCCESS_RESP_CODE = "000000";
    public static final Pattern FIELD_SKIP_SIGN_CHECK_PATTERN = Pattern.compile("reserved_*");
    public static final String HMAC_SHA256 = "HMAC-SHA256";
}
