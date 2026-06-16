package cn.ggsn.rxlight.transaction.pay.fuyou.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import cn.ggsn.rxlight.transaction.pay.fuyou.domain.OrderTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class QueryOrderRequest extends FuyouSignatureBaseRequest {
    @JsonProperty("order_type")
    private OrderTypeEnum orderType;
    @JsonProperty("mchnt_order_no")
    private String transactionId;
}
