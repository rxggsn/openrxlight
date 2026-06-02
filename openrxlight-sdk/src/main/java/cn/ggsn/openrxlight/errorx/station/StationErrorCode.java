package cn.ggsn.openrxlight.errorx.station;

import cn.ggsn.openrxlight.errorx.Constants;
import cn.ggsn.openrxlight.errorx.ErrorCode;
import lombok.Getter;

@Getter
public enum StationErrorCode implements ErrorCode {
    SpaceNotExist(Constants.STMT_BIZ_CODE + 1, "Parking space [%s] does not exist"),
    StationNotExist(Constants.STMT_BIZ_CODE + 2, "Station [%d] does not exist"),
    RangeNotExist(Constants.STMT_BIZ_CODE + 3, "Range [%d] does not exist");

    private final int value;
    private final String message;

    StationErrorCode(int value, String message) {
        this.value = value;
        this.message = message;
    }
}
