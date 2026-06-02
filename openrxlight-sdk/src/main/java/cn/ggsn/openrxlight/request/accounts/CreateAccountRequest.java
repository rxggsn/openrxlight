package cn.ggsn.openrxlight.request.accounts;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import cn.ggsn.openrxlight.request.Required;
import cn.ggsn.openrxlight.request.Validate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CreateAccountRequest implements Validate {
    @Required
    private String displayName;
    @Required
    private Integer accountType;
    @Required
    private List<Integer> roleTypes;
    private List<ExternalAccountInfo> externalAccounts;
    private AccountExtInfo extInfo; // Only required for merchant accounts

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonNaming(com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ExternalAccountInfo implements Validate {
        @Required
        private Integer accountType;
        @Required
        private String externalAccountId;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
    public interface AccountExtInfo {

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MerchantAccountExtInfo implements AccountExtInfo, Validate {
        @Required
        private Integer merchantType;
        @Required
        private String description;
        private String logo;
        private String website;
        @Required
        private String contactNo;
        @Required
        private String address;
        @Required
        private String districtCode;
    }
}
