package cn.ggsn.openrxlight.api;

import java.net.MalformedURLException;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import cn.ggsn.openrxlight.DataCrypto;
import cn.ggsn.openrxlight.DigitalSignature;
import cn.ggsn.openrxlight.httpx.IHttpTransport;
import cn.ggsn.openrxlight.httpx.impl.OkHttpTransport;
import cn.ggsn.openrxlight.token.GlobalTokenManager;
import cn.ggsn.openrxlight.token.ICache;
import cn.ggsn.openrxlight.token.impl.LocalCache;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy.class)
public class OpenRxLightBuilder {
    /*
     * OpenRxLight platform API URL
     */
    private String url;
    /*
     * 
     * Client ID which provided by OpenRxLight platform
     */
    private String clientId;
    /*
     * Client Secret which provided by OpenRxLight platform
     */
    private String clientSecret;
    /*
     * Private Key for generate digital signature which provided by client
     */
    private String signaturePrivKey;
    /*
     * Public Key for verify digital signature which provided by OpenRxLight
     * platform
     */
    private String signaturePubKey;
    @JsonIgnore
    private ICache tokenCache;
    private DigitalSignature digitalSign;
    private DataCrypto dataCrypto;
    @JsonIgnore
    private IHttpTransport httpTransport;

    public OpenRxLightV2 newClientV2() {
        if (StringUtils.isBlank(this.clientId) || StringUtils.isBlank(this.clientSecret)) {
            throw new IllegalArgumentException("ClientId or ClientSecret is empty");
        }

        if (StringUtils.isBlank(this.signaturePrivKey) || StringUtils.isBlank(this.signaturePubKey)) {
            throw new IllegalArgumentException("SignaturePrivKey or SignaturePubKey is empty");
        }

        if (Objects.isNull(this.tokenCache)) {
            // Default use LocalCache
            this.tokenCache = new LocalCache();
        }
        GlobalTokenManager.setCache(this.clientId, this.tokenCache);

        if (Objects.isNull(this.dataCrypto)) {
            // Default use AES_GCM
            this.dataCrypto = DataCrypto.AES_GCM;
        }

        if (Objects.isNull(this.httpTransport)) {
            // Default use OkHttpTransport
            this.httpTransport = new OkHttpTransport();
        }

        if (Objects.isNull(this.digitalSign)) {
            // Default use RSA-SHA256
            this.digitalSign = DigitalSignature.RSA_SHA256;
        }

        try {
            return new OpenRxLightV2(
                    this.clientId,
                    this.clientSecret,
                    this.signaturePrivKey,
                    this.signaturePubKey,
                    this.digitalSign,
                    this.dataCrypto,
                    this.httpTransport,
                    this.url);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid URL format", e);
        }

    }
}
