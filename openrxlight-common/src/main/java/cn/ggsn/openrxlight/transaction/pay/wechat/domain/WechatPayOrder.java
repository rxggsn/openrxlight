package cn.ggsn.openrxlight.transaction.pay.wechat.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.binarywang.wxpay.bean.result.WxPayOrderQueryV3Result;
import com.wechat.pay.java.service.payments.model.Transaction;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Optional;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WechatPayOrder {
    private String transactionId;
    private String appid;
    private String mchId;
    private String counterpartyTxnId;
    private Transaction.TradeStateEnum tradeState;
    private Transaction.TradeTypeEnum tradeType;
    private String tradeStateDesc;
    private Payer payer;
    private LocalDateTime successTime;
    @Setter
    private String counterpartyTraceId;

    public static WechatPayOrder from(WxPayOrderQueryV3Result transaction) {
        return WechatPayOrder.builder()
                .appid(transaction.getAppid())
                .transactionId(transaction.getOutTradeNo())
                .counterpartyTxnId(transaction.getTransactionId())
                .tradeState(Transaction.TradeStateEnum.valueOf(transaction.getTradeState()))
                .tradeType(Optional.ofNullable(transaction.getTradeType())
                        .map(Transaction.TradeTypeEnum::valueOf)
                        .orElse(null))
                .tradeStateDesc(transaction.getTradeStateDesc())
                .payer(Payer.builder()
                        .openId(Optional.ofNullable(transaction.getPayer())
                                .map(WxPayOrderQueryV3Result.Payer::getOpenid)
                                .orElse(null))
                        .build())
                .build();
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Payer {
        @JsonProperty("openid")
        private String openId;
    }
}
