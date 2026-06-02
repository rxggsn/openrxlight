package cn.ggsn.openrxlight.account.external;

import cn.ggsn.openrxlight.domain.AccountType;
import cn.ggsn.openrxlight.domain.ExternalAccountType;
import cn.ggsn.openrxlight.domain.SourceType;
import cn.ggsn.openrxlight.domain.ExternalAccount.ExternalAccountInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class GetExternalAccountReq {
    private String externalAccountId;
    private String authCode;
    private ExternalAccountType externalAccountType;
    private ExternalAccountInfo accountInfo;
    private SourceType sourceType;
    private AccountType accountType;
    private String channelId;
}
