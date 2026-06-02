package cn.ggsn.openrxlight.transaction.pay.wechat.response;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy.class)
public class WxNativePayResponse {
    private String qrCode;
    private String counterpartyTxnId;
    private String counterpartyTraceId;
    private String channelTransactionId;
}
