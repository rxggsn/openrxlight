package cn.ggsn.openrxlight.transaction.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import cn.ggsn.openrxlight.lang.Lists2;
import cn.ggsn.openrxlight.transaction.pay.wechat.domain.WechatPayOrder;
import cn.ggsn.openrxlight.transaction.pay.wechat.domain.WechatRefund;
import lombok.*;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CounterpartyTransaction {

    private String counterpartyTxnId;
    private CounterpartyTransactionInfo counterpartyTransactionInfo;
    @JsonIgnore
    @Setter
    private TransactionStatus status;
    // 与 Transaction.channelTransactionId 一一对应
    @Setter
    private String channelTransactionId;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    public interface CounterpartyTransactionInfo {
        String tryGetCounterpartyTraceId();

        void tryUpdateCounterpartyTraceId(String counterpartyTraceId);
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @EqualsAndHashCode
    public static class WechatPrepayInfo implements CounterpartyTransactionInfo {
        private String appId;
        private String timestamp;
        private String nonceStr;
        @JsonProperty("package")
        private String packageValue;
        private String signType;
        private String paySign;
        @Setter
        private String counterpartyTraceId;

        @Override
        public String tryGetCounterpartyTraceId() {
            return this.counterpartyTraceId;
        }

        @Override
        public void tryUpdateCounterpartyTraceId(String counterpartyTraceId) {
            this.counterpartyTraceId = counterpartyTraceId;
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WechatPayOrderInfo implements CounterpartyTransactionInfo {
        private WechatPayOrder wechatPayOrder;

        @Override
        public String tryGetCounterpartyTraceId() {
            return this.wechatPayOrder.getCounterpartyTraceId();
        }

        @Override
        public void tryUpdateCounterpartyTraceId(String counterpartyTraceId) {
            this.wechatPayOrder.setCounterpartyTraceId(counterpartyTraceId);
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WechatRefundInfo implements CounterpartyTransactionInfo {
        private List<WechatRefund> refunds;

        public boolean isAllRefunded() {
            return Lists2.matchAll(refunds, WechatRefund::isSuccess);
        }

        public boolean hasRefundFailure() {
            return Lists2.anyOf(refunds, WechatRefund::isFailed);
        }

        public boolean isProcessing() {
            return Lists2.anyOf(refunds, WechatRefund::isProcessing);
        }

        @Override
        public String tryGetCounterpartyTraceId() {
            return null;
        }

        @Override
        public void tryUpdateCounterpartyTraceId(String counterpartyTraceId) {

        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NativePayInfo implements CounterpartyTransactionInfo {
        private String qrCode;
        @Setter
        private String counterpartyTraceId;

        @Override
        public String tryGetCounterpartyTraceId() {
            return this.counterpartyTraceId;
        }

        @Override
        public void tryUpdateCounterpartyTraceId(String counterpartyTraceId) {
            this.counterpartyTraceId = counterpartyTraceId;
        }
    }
}
