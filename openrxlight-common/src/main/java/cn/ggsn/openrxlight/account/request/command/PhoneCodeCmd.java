package cn.ggsn.openrxlight.account.request.command;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import cn.ggsn.openrxlight.account.external.GetExternalAccountReq;
import cn.ggsn.openrxlight.domain.AccountType;
import cn.ggsn.openrxlight.domain.ExternalAccountType;
import cn.ggsn.openrxlight.domain.SourceType;
import cn.ggsn.openrxlight.request.Required;
import cn.ggsn.openrxlight.request.Validate;
import cn.ggsn.openrxlight.web.Command;
import lombok.Getter;

@Getter
@JsonNaming(SnakeCaseStrategy.class)
public class PhoneCodeCmd implements Command<GetExternalAccountReq>, Validate {
    @Required
    private String phoneNumber;
    @Required
    private Integer sourceType;
    @Required
    private Integer accountType;

    @Override
    public GetExternalAccountReq toResource() {
        this.validate();
        return GetExternalAccountReq
                .builder()
                .externalAccountId(this.phoneNumber)
                .externalAccountType(ExternalAccountType.PHONE)
                .sourceType(SourceType.fromValue(this.sourceType))
                .accountType(AccountType.fromValue(this.accountType))
                .build();
    }
}
