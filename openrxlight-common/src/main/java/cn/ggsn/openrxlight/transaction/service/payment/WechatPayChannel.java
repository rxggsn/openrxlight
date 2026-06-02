package cn.ggsn.openrxlight.transaction.service.payment;

import java.util.List;
import cn.ggsn.openrxlight.errorx.BizException;
import cn.ggsn.openrxlight.lang.Lists2;
import cn.ggsn.openrxlight.transaction.domain.ChannelType;
import cn.ggsn.openrxlight.transaction.domain.CounterpartyTransaction;
import cn.ggsn.openrxlight.transaction.domain.Transaction;
import cn.ggsn.openrxlight.transaction.domain.TransactionType;
import cn.ggsn.openrxlight.transaction.pay.WechatPayApi;
import cn.ggsn.openrxlight.transaction.pay.wechat.error.WechatPayErrorCode;
import cn.ggsn.openrxlight.transaction.vo.Payment;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Singleton;

@Singleton
public class WechatPayChannel implements PaymentChannel {
    private static final int MAX_REFUND_NUM = 50;
    private final Instance<WechatPayApi> wechatPayApis;
    private final List<PaymentSubProcessor> paymentSubProcessors;

    public WechatPayChannel(@Any Instance<WechatPayApi> wechatPayApis) {
        this.wechatPayApis = wechatPayApis;
        this.paymentSubProcessors = Lists2.of(
                new WechatPaySubProcessor(this.wechatPayApis),
                new WechatRefundSubProcessor(this.wechatPayApis));
    }

    @Override
    public boolean support(TransactionType transactionType, ChannelType channelType) {
        if (this.wechatPayApis.isAmbiguous()) {
            return false;
        }
        return (ChannelType.WECHAT_OFFICIAL_PAY.equals(channelType) || ChannelType.FUIOU_PAY.equals(channelType))
                && (TransactionType.WALLET_DEPOSIT.equals(transactionType) ||
                        TransactionType.WALLET_WITHDRAW.equals(transactionType) ||
                        TransactionType.WECHAT_PAY.equals(transactionType) ||
                        TransactionType.WECHAT_REFUND.equals(transactionType));
    }

    @Override
    public CounterpartyTransaction doPayment(Payment payment) {
        payment.validate();
        return this.selectPaymentProcessor(payment.getTransaction().checkTransactionType())
                .process(payment);
    }

    private PaymentSubProcessor selectPaymentProcessor(TransactionType transactionType) {
        return this.paymentSubProcessors.stream()
                .filter(paymentSubProcessor -> paymentSubProcessor.support(transactionType))
                .findFirst()
                .orElseThrow(
                        () -> new BizException(WechatPayErrorCode.UnsupportedTransactionType, transactionType.name()));
    }

    @Override
    public CounterpartyTransaction getCounterpartyTransaction(Transaction transaction) {
        return this.selectPaymentProcessor(transaction.checkTransactionType())
                .getCounterpartyTransaction(transaction);
    }

    @Override
    public int maximumRefundCount() {
        return MAX_REFUND_NUM;
    }

}
