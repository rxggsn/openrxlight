package cn.ggsn.openrxlight.transaction.pay.wechat.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WechatPrepayResponse {
    private String channelTransactionId;
    private String appId;
    private String timestamp;
    private String nonceStr;
    private String packageValue;
    private String signType;
    private String paySign;
}
