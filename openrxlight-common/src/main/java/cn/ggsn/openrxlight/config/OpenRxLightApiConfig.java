package cn.ggsn.openrxlight.config;

import java.util.Optional;

import io.quarkus.arc.properties.IfBuildProperty;
import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "openrxlight.api")
@IfBuildProperty(name = "openrxlight.api.enabled", stringValue = "true", enableIfMissing = false)
public interface OpenRxLightApiConfig {
    boolean enabled();

    Optional<String> url();

    String clientId();

    String clientSecret();

    String signaturePrivKey();

    String signaturePubKey();
}
