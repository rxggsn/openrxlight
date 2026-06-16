package cn.ggsn.rxlight.transaction.pay.fuyou.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class PrepayResponse extends FuyouSignatureBaseResponse {
    @JsonProperty("sub_mer_id")
    private String subMerchantId;
    @JsonProperty("session_id")
    private String sessionId;
    @JsonProperty("sdk_timestamp")
    private String timestamp;
    @JsonProperty("sdk_noncestr")
    private String nonceStr;
    @JsonProperty("sdk_package")
    private String packageValue;
    @JsonProperty("sdk_signtype")
    private String signType;
    @JsonProperty("sdk_paysign")
    private String paySign;
    @JsonProperty("sub_appid")
    private String appId;
    @JsonProperty("qr_code")
    private String qrCode;
    @JsonProperty("sub_openid")
    private String openId;
    @JsonProperty("sdk_partnerid")
    private String partnerId;
    @JsonProperty("sdk_appid")
    private String sdkAppId;
}
