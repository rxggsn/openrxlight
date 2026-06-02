package cn.ggsn.openrxlight.account.external.impl.wechat;

import me.chanjar.weixin.common.error.WxErrorException;
import org.apache.commons.lang.StringUtils;

import cn.ggsn.openrxlight.account.external.impl.wechat.model.WxRedisConfig;
import cn.ggsn.openrxlight.account.external.impl.wechat.service.WxService;
import cn.ggsn.openrxlight.lang.Maps2;
import io.quarkus.arc.properties.IfBuildProperty;
import io.vertx.mutiny.redis.client.RedisAPI;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Singleton;

import java.util.Map;

@Singleton
@IfBuildProperty(name = "rxlight.wechat.mini-program.enabled", stringValue = "true")
public class WechatMiniProgramRouter {
    private final Map<String, WechatMiniProgramApi> router;
    private final WechatMiniProgramApi defaultWechatMiniProgramApi;

    public WechatMiniProgramRouter(Instance<WechatMiniProgramConfig> wechatMiniProgramConfig, RedisAPI redis)
            throws WxErrorException {
        if (wechatMiniProgramConfig.isResolvable()) {
            WechatMiniProgramConfig config = wechatMiniProgramConfig.get();
            WxRedisConfig wxMaConfig = new WxRedisConfig(redis);
            wxMaConfig.setAppid(config.appId());
            wxMaConfig.setAesKey(config.apiV3Key());
            wxMaConfig.setSecret(config.appSecret());
            this.defaultWechatMiniProgramApi = new WechatMiniProgramApi(new WxService(wxMaConfig),
                    config.appId());
        } else {
            this.defaultWechatMiniProgramApi = null;
        }
        this.router = Maps2.empty();
    }

    public WechatMiniProgramApi findByAppId(String appId) {
        if (StringUtils.isNotBlank(appId) && !this.router.containsKey(appId)) {
            ;
        }
        WechatMiniProgramApi wechatMiniProgramApi = this.router.get(appId);
        if (wechatMiniProgramApi == null) {
            return this.defaultWechatMiniProgramApi;
        } else {
            return wechatMiniProgramApi;
        }
    }
}
