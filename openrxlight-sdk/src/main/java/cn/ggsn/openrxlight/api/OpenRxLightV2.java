package cn.ggsn.openrxlight.api;

import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.lang.StringUtils;

import cn.ggsn.openrxlight.Constants;
import cn.ggsn.openrxlight.DataCrypto;
import cn.ggsn.openrxlight.DigitalSignature;
import cn.ggsn.openrxlight.config.Config;
import cn.ggsn.openrxlight.httpx.IHttpTransport;
import cn.ggsn.openrxlight.services.AuthenticationService;
import cn.ggsn.openrxlight.services.BillingService;
import cn.ggsn.openrxlight.services.DIService;
import cn.ggsn.openrxlight.services.FileService;
import cn.ggsn.openrxlight.services.OrderService;
import cn.ggsn.openrxlight.services.StationService;
import cn.ggsn.openrxlight.services.SystemService;
import lombok.Getter;

public class OpenRxLightV2 {

    private OrderService orderService;
    private DIService diService;
    private StationService stationService;
    private SystemService systemService;
    private FileService fileService;
    private AuthenticationService authenticationService;
    private BillingService billingService;
    @Getter
    private final Config config;

    OpenRxLightV2(String clientId, String clientSecret, String signaturePriKey, String signaturePubKey,
            DigitalSignature digitalSign, DataCrypto dataCrypto, IHttpTransport httpTransport,
            String url) throws MalformedURLException {
        URL parsedUrl = new URL(
                StringUtils.isNotBlank(url) ? url : String.format("%s://%s/v2", Constants.SCHEME, Constants.BASE_URL));
        Config config = Config.builder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .signaturePriKey(signaturePriKey)
                .signaturePubKey(signaturePubKey)
                .httpTransport(httpTransport)
                .digitalSign(digitalSign)
                .dataCrypto(dataCrypto)
                .baseUrl(parsedUrl.getPath())
                .host(parsedUrl.getHost())
                .scheme(parsedUrl.getProtocol())
                .port(parsedUrl.getPort() != -1 ? parsedUrl.getPort() : parsedUrl.getDefaultPort())
                .build();
        this.orderService = new OrderService(config);
        this.diService = new DIService(config);
        this.stationService = new StationService(config);
        this.systemService = new SystemService(config);
        this.authenticationService = new AuthenticationService(config);
        this.fileService = new FileService(config);
        this.billingService = new BillingService(config);
        this.config = config;
    }

    public OrderService orders() {
        return this.orderService;
    }

    public DIService dhforceIntelligence() {
        return this.diService;
    }

    public StationService stations() {
        return this.stationService;
    }

    public SystemService system() {
        return this.systemService;
    }

    public FileService file() {
        return this.fileService;
    }

    public AuthenticationService authentication() {
        return this.authenticationService;
    }

    public BillingService billing() {
        return this.billingService;
    }
}
