package cn.ggsn.openrxlight.account.request;

import cn.ggsn.openrxlight.account.external.GetExternalAccountReq;
import cn.ggsn.openrxlight.account.request.command.EmailCmd;
import cn.ggsn.openrxlight.account.request.command.LoginWeAppCmd;
import cn.ggsn.openrxlight.account.request.command.PhoneCodeCmd;
import cn.ggsn.openrxlight.domain.ExternalAccountType;
import cn.ggsn.openrxlight.web.DispatchCommand;
import lombok.Getter;

@Getter
public class LoginRequest extends DispatchCommand<GetExternalAccountReq> {
    static {
        register(ExternalAccountType.WECHAT_MINI_PROGRAM.getValue(), LoginWeAppCmd.class);
        register(ExternalAccountType.PHONE.getValue(), PhoneCodeCmd.class);
        register(ExternalAccountType.EMAIL.getValue(), EmailCmd.class);
    }
}
