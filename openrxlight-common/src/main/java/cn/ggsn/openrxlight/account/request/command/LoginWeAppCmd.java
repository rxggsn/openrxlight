package cn.ggsn.openrxlight.account.request.command;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import cn.ggsn.openrxlight.account.external.GetExternalAccountReq;
import cn.ggsn.openrxlight.domain.AccountType;
import cn.ggsn.openrxlight.domain.ExternalAccount;
import cn.ggsn.openrxlight.domain.ExternalAccountType;
import cn.ggsn.openrxlight.domain.SourceType;
import cn.ggsn.openrxlight.request.Required;
import cn.ggsn.openrxlight.request.Validate;
import cn.ggsn.openrxlight.web.Command;
import lombok.Getter;

@Getter
@JsonNaming(SnakeCaseStrategy.class)
public class LoginWeAppCmd implements Command<GetExternalAccountReq>, Validate {
    @Required
    private String appId;
    @Required
    private String authorizationCode;
    @Required
    private String externalAccountId;
    @Required
    private Integer sourceType;
    @Required
    private Integer accountType;

    @Override
    public GetExternalAccountReq toResource() {
        return GetExternalAccountReq.builder()
                .externalAccountId(this.externalAccountId)
                .authCode(this.authorizationCode)
                .externalAccountType(ExternalAccountType.WECHAT_MINI_PROGRAM)
                .accountInfo(ExternalAccount.WechatAccountInfo.builder()
                        .sessionKey(null)
                        .unionId(null)
                        .appId(this.appId)
                        .build())
                .accountType(AccountType.fromValue(this.accountType))
                .sourceType(SourceType.fromValue(this.sourceType))
                .build();

    }
}
