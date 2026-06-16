package cn.ggsn.openrxlight.domain;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(SnakeCaseStrategy.class)
@EqualsAndHashCode(callSuper = false, of = {
        "externalAccountId", "accountType"
})
public class ExternalAccount {
    private String externalAccountId;
    private ExternalAccountType accountType;
    private ExternalAccountInfo accountInfo;
    private String accountSecret;
    @JsonIgnore
    @Setter
    private UUID boundAccountId;

    public void securePrivateInfo() {
        this.externalAccountId = null;
        this.accountInfo = null;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
    public interface ExternalAccountInfo {
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class WechatAccountInfo implements ExternalAccountInfo {
        private String appId;
        private String unionId;
        private String sessionKey;
        @Setter
        private String phoneNo;
    }

}
