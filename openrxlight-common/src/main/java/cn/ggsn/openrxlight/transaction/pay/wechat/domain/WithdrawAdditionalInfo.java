package cn.ggsn.openrxlight.transaction.pay.wechat.domain;

import lombok.*;
import org.apache.commons.lang3.StringUtils;

import cn.ggsn.openrxlight.lang.Lists2;
import cn.ggsn.openrxlight.model.Currency;
import cn.ggsn.openrxlight.request.Required;
import cn.ggsn.openrxlight.utils.UUIdConverter;
import cn.ggsn.openrxlight.transaction.pay.domain.TransactionAdditionalInfo;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class WithdrawAdditionalInfo implements TransactionAdditionalInfo {
    @Required
    @Setter
    private List<WithdrawInfo> withdrawInfos;

    public List<WithdrawInfo> mergeRefund(Currency ccy) {
        if (Lists2.isEmpty(withdrawInfos)) {
            return null;
        }

        this.withdrawInfos.sort(Comparator.comparing(WithdrawInfo::getCompletedTime).reversed());
        final Currency[] remainingCcy = { ccy.clone() };
        List<WithdrawInfo> result = Lists2.empty();
        Lists2.foreach(this.withdrawInfos, withdrawInfo -> {
            if (!remainingCcy[0].equals(Currency.ZERO)) {
                WithdrawInfo cloned = withdrawInfo.clone();
                if (cloned.getOriginCcy().compareTo(remainingCcy[0]) < 0) {
                    cloned.setWithdrawCcy(withdrawInfo.getOriginCcy().clone());
                    cloned.setRefundId(cn.ggsn.openrxlight.lang.UUID.randomUUID());
                    result.add(cloned);
                    remainingCcy[0].subtract(cloned.getOriginCcy());
                } else {
                    cloned.setWithdrawCcy(remainingCcy[0]);
                    cloned.setRefundId(cn.ggsn.openrxlight.lang.UUID.randomUUID());
                    result.add(cloned);
                    remainingCcy[0] = Currency.ZERO;
                }
            }
        });

        if (!remainingCcy[0].equals(Currency.ZERO)) {
            return null;
        }
        return result;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class WithdrawInfo implements Cloneable {
        private String counterpartyTxnId;
        private UUID transactionId;
        private Currency originCcy;
        @Setter
        private Currency withdrawCcy;
        private LocalDateTime completedTime;
        private UUID refundId;
        private String channelTransactionId;

        public void setRefundId(UUID refundId) {
            if (this.refundId == null) {
                this.refundId = refundId;
            }
        }

        public String getWechatOutTradeNo() {
            if (StringUtils.isNotBlank(this.channelTransactionId)) {
                return this.channelTransactionId;
            } else {
                return UUIdConverter.replaceHyphenWithEmptyChar(this.transactionId);
            }
        }

        @Override
        public WithdrawInfo clone() {
            try {
                return (WithdrawInfo) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
