package cn.ggsn.rxlight.transaction.pay.fuyou.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import cn.ggsn.rxlight.transaction.pay.fuyou.domain.TradeTypeEnum;
import lombok.Getter;

@Getter
public class FuyouSignaturePrepayOrderNotification extends FuyouSignatureBaseResponse {
    @JsonProperty("order_amt")
    private int amount;
    @JsonProperty("settle_order_amt")
    private int actualAmount;
    @JsonProperty("curr_type")
    private String currency;
    @JsonProperty("transaction_id")
    private String counterpartyTxnId;
    @JsonProperty("mchnt_order_no")
    private String transactionId;
    @JsonProperty("order_type")
    private TradeTypeEnum orderType;
    @JsonProperty("txn_fin_ts")
    private String completedAt;
    @JsonProperty("user_id")
    private String userId;
}
