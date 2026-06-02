package cn.ggsn.openrxlight.transaction.service.payment;

import java.util.List;

import cn.ggsn.openrxlight.errorx.BizException;
import cn.ggsn.openrxlight.lang.Lists2;
import cn.ggsn.openrxlight.transaction.domain.ChannelType;
import cn.ggsn.openrxlight.transaction.domain.CounterpartyTransaction;
import cn.ggsn.openrxlight.transaction.domain.Transaction;
import cn.ggsn.openrxlight.transaction.domain.TransactionStatus;
import cn.ggsn.openrxlight.transaction.domain.TransactionType;
import cn.ggsn.openrxlight.transaction.pay.WechatPayApi;
import cn.ggsn.openrxlight.transaction.pay.wechat.domain.WechatRefund;
import cn.ggsn.openrxlight.transaction.pay.wechat.domain.WechatRefundAdditionalInfo;
import cn.ggsn.openrxlight.transaction.pay.wechat.domain.WechatRefundQuery;
import cn.ggsn.openrxlight.transaction.pay.wechat.domain.WithdrawAdditionalInfo;
import cn.ggsn.openrxlight.transaction.pay.wechat.error.WechatPayErrorCode;
import cn.ggsn.openrxlight.transaction.vo.Payment;
import jakarta.enterprise.inject.Instance;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class WechatRefundSubProcessor implements PaymentSubProcessor {
    private final Instance<WechatPayApi> wechatPayApis;

    @Override
    public CounterpartyTransaction process(Payment payment) {
        ChannelType channelType = payment.getTransaction().getTransactionInfo().getDestination()
                .getCounterpartyAccount().getChannelType();
        WechatPayApi wechatPayApi = this.selectWechatPayApi(payment.getTransaction().checkTransactionType(),
                channelType);

        if (wechatPayApi == null) {
            throw new BizException(
                    WechatPayErrorCode.UnsupportedChannelType, channelType.name());
        }

        if (TransactionType.WALLET_WITHDRAW.equals(payment.getTransaction().checkTransactionType())) {
            var refundInfo = (WithdrawAdditionalInfo) payment.getTransactionAdditionalInfo();
            List<WechatRefund> wechatRefunds = Lists2.map(refundInfo.getWithdrawInfos(), refund -> {
                return wechatPayApi.refund(
                        refund.getWechatOutTradeNo(),
                        refund.getWithdrawCcy(),
                        refund.getOriginCcy(),
                        refund.getRefundId(),
                        payment.getTransaction().getTransactionInfo().getDestination());
            });

            CounterpartyTransaction.WechatRefundInfo wechatRefundInfo = new CounterpartyTransaction.WechatRefundInfo(
                    wechatRefunds);
            TransactionStatus status = TransactionStatus.REFUNDING;
            if (wechatRefundInfo.isAllRefunded()) {
                status = TransactionStatus.SUCCESS;
            } else if (wechatRefundInfo.hasRefundFailure()) {
                status = TransactionStatus.FAILURE;
            }

            return new CounterpartyTransaction(
                    null,
                    wechatRefundInfo,
                    status,
                    null);
        } else if (TransactionType.WECHAT_REFUND.equals(payment.getTransaction().checkTransactionType())) {
            var refund = (WechatRefundAdditionalInfo) payment.getTransactionAdditionalInfo();
            WechatRefund wechatRefund = wechatPayApi.refund(
                    refund.getOriginWechatOutTradeNo(),
                    payment.getTransaction().getCcy(),
                    refund.getOriginCcy(),
                    payment.getTransaction().getTransactionId(),
                    payment.getTransaction().getTransactionInfo().getDestination());
            TransactionStatus status = TransactionStatus.REFUNDING;
            if (wechatRefund.isFailed()) {
                status = TransactionStatus.FAILURE;
            } else if (wechatRefund.isSuccess()) {
                status = TransactionStatus.SUCCESS;
            }
            return new CounterpartyTransaction(
                    null,
                    new CounterpartyTransaction.WechatRefundInfo(List.of(wechatRefund)),
                    status,
                    refund.getOriginChannelTransactionId());
        } else {
            return null;
        }
    }

    @Override
    public boolean support(TransactionType transactionType) {
        return TransactionType.WECHAT_REFUND.equals(transactionType) ||
                TransactionType.WALLET_WITHDRAW.equals(transactionType);
    }

    @Override
    public CounterpartyTransaction getCounterpartyTransaction(Transaction transaction) {
        CounterpartyTransaction.WechatRefundInfo refundInfo = (CounterpartyTransaction.WechatRefundInfo) transaction
                .getTransactionInfo()
                .getCounterpartyTransactionInfo();

        ChannelType channelType = transaction
                .getTransactionInfo()
                .getDestination()
                .getCounterpartyAccount()
                .getChannelType();
        WechatPayApi wechatPayApi = this.selectWechatPayApi(transaction.checkTransactionType(), channelType);

        if (wechatPayApi == null) {
            throw new BizException(
                    WechatPayErrorCode.UnsupportedChannelType, channelType.name());
        }

        refundInfo.getRefunds().forEach(refund -> {
            WechatRefund wechatRefund = wechatPayApi.queryRefund(
                    WechatRefundQuery
                            .builder()
                            .refundTxnId(refund.getRefundTxnId())
                            .build());
            refund.setStatus(wechatRefund.getStatus());
        });

        CounterpartyTransaction counterpartyTransaction = new CounterpartyTransaction(
                transaction.getCounterpartyTxnId(),
                refundInfo,
                transaction.checkTransactionStatus(),
                transaction.getRxLightOrderIdInCounterparty());
        if (refundInfo.isProcessing()) {
            counterpartyTransaction.setStatus(TransactionStatus.REFUNDING);
        } else if (refundInfo.isAllRefunded()) {
            counterpartyTransaction.setStatus(TransactionStatus.SUCCESS);
        } else if (refundInfo.hasRefundFailure()) {
            counterpartyTransaction.setStatus(TransactionStatus.REFUND_FAILURE);
        }

        return counterpartyTransaction;
    }

    private WechatPayApi selectWechatPayApi(TransactionType transactionType, ChannelType channelType) {
        return this.wechatPayApis.stream()
                .filter(wechatPayApi -> wechatPayApi.supports(transactionType, channelType))
                .findFirst()
                .orElseThrow(() -> new BizException(WechatPayErrorCode.UnsupportedChannelType, channelType.name()));
    }

}
