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
public class RefundRequest extends FuyouSignatureBaseRequest {
    @JsonProperty("order_type")
    private OrderTypeEnum orderType;
    @JsonProperty("refund_order_no")
    @Setter
    private String refundTxnId;
    @JsonProperty("mchnt_order_no")
    private String transactionId;
    @JsonProperty("total_amt")
    private int totalAmount;
    @JsonProperty("refund_amt")
    private int refundAmount;
    @JsonProperty("operator_id")
    private String operatorId;
}
