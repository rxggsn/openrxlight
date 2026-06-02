package cn.ggsn.openrxlight.account.domain;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import cn.ggsn.openrxlight.domain.ExternalAccount.ExternalAccountInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy.class)
public class EmailAccountInfo implements ExternalAccountInfo {
    private String password;
    private String salt;
}
