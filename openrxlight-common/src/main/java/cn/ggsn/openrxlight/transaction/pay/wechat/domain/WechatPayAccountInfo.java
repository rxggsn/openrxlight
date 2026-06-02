package cn.ggsn.openrxlight.transaction.pay.wechat.domain;

import lombok.Getter;
import cn.ggsn.openrxlight.request.Required;
import cn.ggsn.openrxlight.transaction.pay.domain.CounterpartyAccount;

@Getter
public class WechatPayAccountInfo extends CounterpartyAccount {
    @Required
    private String appId;
    private String serviceId;
    private String mchId;
    private String apiV3Key;
    private String privateKey;
    private String merchantSerialNumber;
    private String privateCertPath;
    private String notifyUrl;

    @Override
    public String getCounterpartyAccountId() {
        return this.appId;
    }
}
