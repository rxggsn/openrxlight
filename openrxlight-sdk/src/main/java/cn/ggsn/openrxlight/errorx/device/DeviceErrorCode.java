package cn.ggsn.openrxlight.errorx.device;

import cn.ggsn.openrxlight.errorx.Constants;
import cn.ggsn.openrxlight.errorx.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DeviceErrorCode implements ErrorCode {
    DeviceNotExist(Constants.DEVICE_BIZ_CODE + 1, "device [%d] not found")

    ;
    private final int value;
    private final String message;
}
