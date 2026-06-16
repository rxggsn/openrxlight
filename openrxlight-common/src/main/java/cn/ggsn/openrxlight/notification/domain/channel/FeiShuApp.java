package cn.ggsn.openrxlight.notification.domain.channel;

import java.util.List;

import cn.ggsn.openrxlight.lang.Lists2;
import cn.ggsn.openrxlight.notification.domain.ChannelConfiguration;
import cn.ggsn.openrxlight.notification.domain.NtySceneType;
import lombok.Getter;

@Getter
public class FeiShuApp implements ChannelConfiguration.ChannelAccount {

    private String appId;
    private String appSecret;
    private List<Short> sceneTypes;

    @Override
    public boolean supports(NtySceneType sceneType) {
        return Lists2.anyOf(this.sceneTypes, ty -> ty == sceneType.getCode());
    }

}
