package cn.ggsn.openrxlight.account.request;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import cn.ggsn.openrxlight.request.Required;
import cn.ggsn.openrxlight.request.Validate;
import lombok.Getter;

@Getter
@JsonNaming(com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy.class)
public class VerificationCodeRequest implements Validate {
    @Required
    private String externalAccountId;
    @Required
    private Integer sourceType;
    @Required
    private Integer accountType;
    @Required
    private Integer externalAccountType;
}
