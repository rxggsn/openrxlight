package cn.ggsn.openrxlight.errorx.order;

import cn.ggsn.openrxlight.errorx.Constants;
import cn.ggsn.openrxlight.errorx.ErrorCode;
import lombok.Getter;

@Getter
public enum OrderErrorCode implements ErrorCode {
    NotFoundReservation(Constants.ORDER_BIZ_CODE + 1, "Reservation [%d] not found"),
    CannotCancel(Constants.ORDER_BIZ_CODE + 2, "Reservation [%d] cannot be cancelled"),
    CreateOrderUpsupported(Constants.ORDER_BIZ_CODE + 3, "Create order unsupported"),
    DispatchError(Constants.ORDER_BIZ_CODE + 4, "Dispatch error: %s"),
    CannotFinishOrder(Constants.ORDER_BIZ_CODE + 5, "Cannot finish order [%d]"),
    NotFoundOrder(Constants.ORDER_BIZ_CODE + 6, "Order [%s] not found"),
    NoDispatchJobForOrder(Constants.ORDER_BIZ_CODE + 7, "Order with dispatch job id [%d] not found"),
    ;

    private final int value;
    private final String message;

    OrderErrorCode(int code, String message) {
        this.value = code;
        this.message = message;
    }
}
