package cn.ggsn.openrxlight.transaction.pay.wechat;

import com.github.binarywang.wxpay.service.WxPayService;

import cn.ggsn.openrxlight.lang.Maps2;
import java.util.Map;
import java.util.UUID;

public class CounterpartyWeChatPayConfigContainer {
    private final Map<UUID, WechatPayConfig> wechatPayConfigMap = Maps2.empty();
    private final Map<UUID, WxPayService> wxPayServiceMap = Maps2.empty();

    public WechatPayConfig getWechatPayConfig(UUID counterpartyId) {
        return this.wechatPayConfigMap.get(counterpartyId);
    }

    public void addConfig(UUID accountId, WechatPayConfig config) {
        this.wechatPayConfigMap.put(accountId, config);
        // this.wxPayServiceMap.put(accountId, config.createWxPayService());
    }

    public void addWxPayService(UUID accountId, WxPayService wxPayService) {
        this.wxPayServiceMap.put(accountId, wxPayService);
    }

    public boolean containsWxService(UUID accountId) {
        return this.wxPayServiceMap.containsKey(accountId);
    }

    public WxPayService getWxService(UUID accountId) {
        return this.wxPayServiceMap.get(accountId);
    }
}
