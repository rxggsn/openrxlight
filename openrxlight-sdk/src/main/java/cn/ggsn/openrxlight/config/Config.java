package cn.ggsn.openrxlight.config;

import cn.ggsn.openrxlight.DataCrypto;
import cn.ggsn.openrxlight.DigitalSignature;
import cn.ggsn.openrxlight.httpx.IHttpTransport;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Config {
    private String clientId;
    private String clientSecret;
    private String signaturePriKey;
    private String signaturePubKey;
    private IHttpTransport httpTransport;
    private DigitalSignature digitalSign;
    private DataCrypto dataCrypto;
    private String baseUrl;
    private String host;
    private String scheme;
    private int port;
}
