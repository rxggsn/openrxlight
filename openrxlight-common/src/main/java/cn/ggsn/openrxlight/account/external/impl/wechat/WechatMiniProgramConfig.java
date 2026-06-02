package cn.ggsn.openrxlight.account.external.impl.wechat;

import io.quarkus.arc.properties.IfBuildProperty;

// import java.util.UUID;

import io.smallrye.config.ConfigMapping;

@IfBuildProperty(name = "rxlight.wechat.mini-program.enabled", stringValue = "true")
@ConfigMapping(prefix = "rxlight.wechat.mini-program")
public interface WechatMiniProgramConfig {
    String appId();

    String appSecret();

    String apiV3Key();

    // UUID counterpartyId();

    String serviceId();

    Boolean enabled();
}
