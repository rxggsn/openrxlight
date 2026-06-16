package cn.ggsn.rxlight.transaction.pay.fuyou.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import cn.ggsn.rxlight.transaction.pay.fuyou.domain.OrderTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreCreateRequest extends FuyouSignatureBaseRequest {
    @JsonProperty("mchnt_order_no")
    @Setter
    private String orderId;
    @JsonProperty("order_type")
    private OrderTypeEnum orderType;
    @JsonProperty("goods_des")
    private String goodsDes;
    @JsonProperty("notify_url")
    private String notifyUrl;
    @JsonProperty("order_amt")
    private Integer amount;
    @JsonProperty("term_ip")
    private String termIp;
    @JsonProperty("txn_begin_ts")
    private String txnBeginTs;
    @JsonProperty("curr_type")
    private String currType;
    @JsonProperty("addn_inf")
    private String addnInf;
    @JsonProperty("goods_detail")
    private String goodsDetail;
    @JsonProperty("goods_tag")
    private String goodsTag;
}
