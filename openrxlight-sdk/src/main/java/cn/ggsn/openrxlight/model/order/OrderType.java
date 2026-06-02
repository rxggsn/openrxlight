package cn.ggsn.openrxlight.model.order;

import lombok.AllArgsConstructor;
import lombok.Getter;

//订单类型，0-未知，1-V2V订单(兼容可以不传入)，2-V2G订单
@Getter
@AllArgsConstructor
public enum OrderType {
    V2V((short) 0, "v2v订单"),
    V2G((short) 1, "v2G订单"),
    ;

    private final short value;
    private final String message;

    public static OrderType fromValue(int payType) {
        for (OrderType type : OrderType.values()) {
            if (type.value == payType) {
                return type;
            }
        }
        return V2V;
    }
}
