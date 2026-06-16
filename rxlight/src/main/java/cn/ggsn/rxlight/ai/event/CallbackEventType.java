package cn.ggsn.rxlight.ai.event;

public interface CallbackEventType {
    public static final String EVENT_PARAMETER = "parameters";
    public static final String CREATE_STATION = "create_station";
    public static final String CANCEL = "cancel";
    public static final String CONFIRM = "confirm";
    public static final String CREATE_DEVICE = "create_device";
    public static final String BIND_DEVICE = "bind_device";
    public static final String BIND_SUPPORTER = "bind_supporter";
    public static final String CONFIRM_BUSINESS_ANALYSIS = "confirm_business_analysis";
    public static final String QUERY_ORDERS = "query_orders";
    public static final String QUERY_STATIONS = "query_stations";
    public static final String QUERY_DEVICES = "query_devices";
    public static final String PAY_FOR_BILL = "pay_for_bill";
    public static final String PAY_FOR_ADDED_ON = "pay_for_added_on";
    public static final String UPGRADE_CREDIT_PLAN = "upgrade_credit_plan";
    public static final String BUY_ADDED_ON = "buy_added_on";
    public static final String CUSTOM_CMD = "custom_command";
}
