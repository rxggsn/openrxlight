package cn.ggsn.openrxlight.transaction.pay.wechat.domain;

import cn.ggsn.openrxlight.transaction.pay.domain.CounterpartyAdditionalInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class WechatCounterpartyAdditionalInfo implements CounterpartyAdditionalInfo {
    private String mchId;
    private String apiV3Key;
    private String privateKey;
    private String merchantSerialNumber;
    private String privateCertPath;
    private String notifyUrl;
}
