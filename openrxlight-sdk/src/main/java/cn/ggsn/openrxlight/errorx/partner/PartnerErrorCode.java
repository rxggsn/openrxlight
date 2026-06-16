package cn.ggsn.openrxlight.errorx.partner;

import cn.ggsn.openrxlight.errorx.Constants;
import cn.ggsn.openrxlight.errorx.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PartnerErrorCode implements ErrorCode {
    ClientNotExist(Constants.PARTNER_BIZ_CODE + 1, "Client not exist"),
    NotSupportedProtocol(Constants.PARTNER_BIZ_CODE + 2, "Not supported protocol"),
    AppNotRegistered(Constants.PARTNER_BIZ_CODE + 3, "App [%s] not registered"),
    PartnerNotFound(Constants.PARTNER_BIZ_CODE + 4, "Partner [%d] not found"),
    ;

    private final int value;
    private final String message;

}
