package cn.ggsn.openrxlight.event.chat;

public interface CallbackEventType {
    public static final String EVENT_PARAMETER = "parameters";
    public static final String CREATE_STATION = "create_station";
    public static final String CANCEL = "cancel";
    public static final String CONFIRM = "confirm";
    public static final String CREATE_DEVICE = "create_device";
    public static final String CREATE_DEVICE_TYPE = "create_device_type";
    public static final String CREATE_ACTIVITY = "create_activity";
    public static final String CONFIRM_ADD_FEE = "confirm_add_fee";
    public static final String INSERT_FEE = "add_fee_config";
    public static final String CREATE_DICT_GROUP = "create_dict_group";
    public static final String INSERT_DICT = "add_dict";
    public static final String CREATE_DICT = "create_dict";
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
}
