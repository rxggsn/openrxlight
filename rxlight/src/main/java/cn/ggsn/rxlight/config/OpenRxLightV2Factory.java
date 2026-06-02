package cn.ggsn.rxlight.config;

import cn.ggsn.openrxlight.api.OpenRxLightBuilder;
import cn.ggsn.openrxlight.api.OpenRxLightV2;
import cn.ggsn.openrxlight.config.OpenRxLightApiConfig;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@Startup
class OpenRxLightV2Factory {

    @Produces
    public OpenRxLightV2 create(@Any Instance<OpenRxLightApiConfig> config) {
        if (config.isUnsatisfied()) {
            log.warn("OpenRxLightApiConfig is not provided, OpenRxLightV2 will not be created");
            return null;
        }
        OpenRxLightApiConfig apiConfig = config.get();
        try {

            return OpenRxLightBuilder
                    .builder()
                    .clientId(apiConfig.clientId())
                    .clientSecret(apiConfig.clientSecret())
                    .signaturePrivKey(apiConfig.signaturePrivKey())
                    .signaturePubKey(apiConfig.signaturePubKey())
                    .url(apiConfig.url().orElse(null))
                    .build()
                    .newClientV2();
        } catch (Exception e) {
            log.error("Failed to create OpenRxLightV2 instance, error={}", e.getMessage(), e);
            throw new RuntimeException("Failed to create OpenRxLightV2 instance", e);
        }
    }
}
