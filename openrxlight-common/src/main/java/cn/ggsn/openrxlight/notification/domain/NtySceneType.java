package cn.ggsn.openrxlight.notification.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NtySceneType {
    CREATE_RESERVATION((short) 1, "预约单创建通知"),
    MODIFY_RESERVATION((short) 2, "预约单变更通知"),
    CANCEL_RESERVATION((short) 3, "预约单取消通知"),
    ORDER_START_CHARGE((short) 4, "订单开始服务通知"),
    ORDER_END_CHARGE((short) 5, "订单结束服务通知"),
    VERIFY_CODE((short) 6, "验证码通知"),
    WEBHOOK((short) 7, "Webhook通知"),
    DI_CALLBACK((short) 8, "DHForce AI 回调通知"),
    ADMIN_NTY((short) 9, "管理员通知"),
    ;

    private final short code;
    private final String description;

    public static NtySceneType fromCode(short code) {
        for (NtySceneType type : NtySceneType.values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid NtySceneType code: " + code);
    }
}
