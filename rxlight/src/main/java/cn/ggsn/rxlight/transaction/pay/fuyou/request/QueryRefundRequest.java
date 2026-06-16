package cn.ggsn.rxlight.transaction.pay.fuyou.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueryRefundRequest extends FuyouSignatureBaseRequest {
    @JsonProperty("refund_order_no")
    private String refundTransactionId;
}
