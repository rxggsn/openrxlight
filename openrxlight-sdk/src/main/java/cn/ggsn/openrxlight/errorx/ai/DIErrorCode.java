package cn.ggsn.openrxlight.errorx.ai;

import cn.ggsn.openrxlight.errorx.Constants;
import cn.ggsn.openrxlight.errorx.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DIErrorCode implements ErrorCode {
    BatchRequestMustBeSame(Constants.DI_BIZE_CODE + 1, "batch request should be in same arguments");

    private final int value;
    private final String message;
}
