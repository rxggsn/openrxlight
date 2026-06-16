package cn.ggsn.rxlight.transaction.pay.fuyou.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QueryCreditOrderRequest extends FuyouSignatureBaseRequest {
    @JsonProperty("mchnt_order_no")
    private String transactionId;

    @Override
    public boolean needSignCheck(String key) {
        return super.needSignCheck(key) && !"term_id".equals(key);
    }
}
