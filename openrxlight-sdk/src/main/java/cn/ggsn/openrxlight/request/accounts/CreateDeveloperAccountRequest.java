package cn.ggsn.openrxlight.request.accounts;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import cn.ggsn.openrxlight.DataCrypto;
import cn.ggsn.openrxlight.DigitalSignature;
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
public class CreateDeveloperAccountRequest implements Validate {
    @Required
    private DataCrypto dataCrypto;
    @Required
    private DigitalSignature digitalSignature;
    @Required
    private String webhookUrl;
    @Required
    private String publicKey;
    // @Required
    // private String developerEmail;
}
