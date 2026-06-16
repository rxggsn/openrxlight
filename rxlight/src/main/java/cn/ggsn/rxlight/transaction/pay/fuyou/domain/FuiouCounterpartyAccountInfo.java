package cn.ggsn.rxlight.transaction.pay.fuyou.domain;

import java.security.PrivateKey;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.wechat.pay.java.core.util.PemUtil;

import cn.ggsn.openrxlight.transaction.pay.domain.CounterpartyAccount;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy.class)
public class FuiouCounterpartyAccountInfo extends CounterpartyAccount {
    private String insCd;
    private String mchntCd;
    private String privateKey;
    private String publicKey;
    private String prepayNotifyUrl;
    private String creditOrderNotifyUrl;
    private String ductionNotifyUrl;
    private String domainName;
    private String orderPrefix;
    private String wxAppId;
    private String preCreateNotifyUrl;

    @Override
    public String getCounterpartyAccountId() {
        return this.mchntCd;
    }

    public PrivateKey generatePrivateKey() {
        return PemUtil.loadPrivateKeyFromString(this.privateKey);
    }

}
