package cn.ggsn.rxlight.transaction.pay.fuyou.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import cn.ggsn.rxlight.transaction.pay.fuyou.domain.OrderTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class PreCreateResponse extends FuyouSignatureBaseResponse {
    @JsonProperty("qr_code")
    private String qrCode;
    @JsonProperty("order_type")
    private OrderTypeEnum orderType;
    @JsonProperty("reserved_fy_trace_no")
    private String counterpartyTxnId;
    @JsonProperty("session_id")
    private String sessionId;
}
