package cn.ggsn.rxlight.transaction.pay.fuyou.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wechat.pay.java.service.payments.model.Transaction;

import cn.ggsn.rxlight.transaction.pay.fuyou.domain.OrderTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QueryOrderResponse extends FuyouSignatureBaseResponse {
    @JsonProperty("buyer_id")
    private String buyerId;
    @JsonProperty("order_type")
    private OrderTypeEnum orderType;
    @JsonProperty("trans_stat")
    private Transaction.TradeStateEnum tradeState;
    @JsonProperty("order_amt")
    private BigDecimal amount;
    @JsonProperty("transaction_id")
    private String counterpartyTxnId;
    @JsonProperty("mchnt_order_no")
    private String transactionId;
    @JsonProperty("addn_inf")
    private String additionalInfo;
    @JsonProperty("reserved_fy_trace_no")
    private String counterpartyTraceId;
}
