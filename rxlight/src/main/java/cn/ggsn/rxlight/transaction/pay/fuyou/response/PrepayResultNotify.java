package cn.ggsn.rxlight.transaction.pay.fuyou.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class PrepayResultNotify extends FuyouSignatureBaseResponse {
    @JsonProperty("user_id")
    private String userId;
    @JsonProperty("order_amt")
    private int amount;
    @JsonProperty("settle_order_amt")
    private int settleOrderAmount;
    @JsonProperty("transaction_id")
    private String counterpartyTxnId;
    @JsonProperty("mchnt_order_no")
    private String transactionId;
    @JsonProperty("txn_fin_ts")
    private String completedAt;
    @JsonProperty("reserved_fy_trace_no")
    private String counterpartyTraceId;
}
