package cn.ggsn.openrxlight.account.external.impl.wechat.service;

import cn.ggsn.openrxlight.account.external.impl.wechat.model.WxConfig;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class WxService {
    private WxUserService wxUserService;
    private final WxConfig wxMaConfig;

    public WxUserService getUserService() {
        if (this.wxUserService == null) {
            this.wxUserService = new WxUserService(this.wxMaConfig);
        }
        return this.wxUserService;
    }
}
