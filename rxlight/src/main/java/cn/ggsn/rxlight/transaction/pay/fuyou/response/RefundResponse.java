package cn.ggsn.rxlight.transaction.pay.fuyou.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import cn.ggsn.rxlight.transaction.pay.fuyou.domain.OrderTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundResponse extends FuyouSignatureBaseResponse {
    @JsonProperty("order_type")
    private OrderTypeEnum orderType;
    @JsonProperty("mchnt_order_no")
    private String transactionId;
    @JsonProperty("refund_order_no")
    private String refundTxnId;
    @JsonProperty("transaction_id")
    private String counterpartyTxnId;
    @JsonProperty("refund_id")
    private String counterpartyRefundTxnId;
    @JsonProperty("reserved_fy_trace_no")
    private String traceId;
}
