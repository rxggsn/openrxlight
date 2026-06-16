package cn.ggsn.rxlight.transaction.pay.fuyou.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import cn.ggsn.rxlight.transaction.pay.fuyou.domain.TradeTypeEnum;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrePayRequest extends FuyouSignatureBaseRequest {
    @JsonProperty("trade_type")
    private TradeTypeEnum tradeType;
    @JsonProperty("goods_des")
    private String goodsDes;
    @JsonProperty("goods_detail")
    private String goodsDetail;
    @JsonProperty("mchnt_order_no")
    @Setter
    private String orderId;
    @JsonProperty("order_amt")
    private int amount;
    @JsonProperty("term_ip")
    private String termIp;
    @JsonProperty("txn_begin_ts")
    private String createdTime;
    @JsonProperty("notify_url")
    private String notifyUrl;
    @JsonProperty("sub_openid")
    private String subOpenId;
    @JsonProperty("sub_appid")
    private String subAppId;
    @JsonProperty("product_id")
    private String productId;
    @JsonProperty("goods_tag")
    private String goodsTag;
    @JsonProperty("addn_inf")
    private String additionalInfo;
    @JsonProperty("curr_type")
    private String currencyType;
    @JsonProperty("limit_pay")
    private String limitPay;
    @JsonProperty("openid")
    private String openId;
}
