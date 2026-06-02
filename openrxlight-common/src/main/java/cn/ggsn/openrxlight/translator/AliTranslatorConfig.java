package cn.ggsn.openrxlight.translator;

import io.quarkus.arc.properties.IfBuildProperty;
import io.smallrye.config.ConfigMapping;

@IfBuildProperty(name = "openrxlight.ai.translator.type", stringValue = "alicloud")
@ConfigMapping(prefix = "openrxlight.ai.translator.alicloud")
public interface AliTranslatorConfig {
    String accessKey();

    String secretKey();

    String endpoint();

    String regionId();
}
