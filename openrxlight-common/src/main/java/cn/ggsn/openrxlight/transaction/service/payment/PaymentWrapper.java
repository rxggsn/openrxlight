package cn.ggsn.openrxlight.transaction.service.payment;

import java.util.Optional;

import cn.ggsn.openrxlight.errorx.BizException;
import cn.ggsn.openrxlight.transaction.domain.CounterpartyTransaction;
import cn.ggsn.openrxlight.transaction.domain.Transaction;
import cn.ggsn.openrxlight.transaction.domain.TransactionType;
import cn.ggsn.openrxlight.transaction.error.TpsErrorCode;
import cn.ggsn.openrxlight.transaction.pay.domain.Counterparty;
import cn.ggsn.openrxlight.transaction.pay.domain.CounterpartyAccount;
import cn.ggsn.openrxlight.transaction.pay.domain.TransactionAdditionalInfo;
import cn.ggsn.openrxlight.transaction.pay.wechat.domain.WithdrawAdditionalInfo;
import cn.ggsn.openrxlight.transaction.vo.Payment;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Singleton;

@Singleton
public class PaymentWrapper {

    private final Instance<PaymentChannel> channelServices;

    public PaymentWrapper(@Any Instance<PaymentChannel> channelServices) {
        this.channelServices = channelServices;
    }

    public CounterpartyTransaction doPayment(Transaction transaction,
            TransactionAdditionalInfo additionalInfo) {
        Counterparty destination = transaction.getTransactionInfo().getDestination();

        PaymentChannel channelService = selectChannelService(transaction.checkTransactionType(),
                destination.getCounterpartyAccount());
        if (transaction.isWithdraw()) {
            var withdrawAdditionalInfo = (WithdrawAdditionalInfo) additionalInfo;
            if (withdrawAdditionalInfo.getWithdrawInfos().size() >= channelService.maximumRefundCount()) {
                throw new BizException(TpsErrorCode.TooManyRefunds);
            }
        }

        CounterpartyTransaction counterpartyTxn = channelService.doPayment(Payment.builder()
                .transactionAdditionalInfo(additionalInfo)
                .transaction(transaction)
                .build());
        transaction.setCounterpartyTransaction(counterpartyTxn);
        return counterpartyTxn;
    }

    public PaymentChannel selectChannelService(TransactionType transactionType,
            CounterpartyAccount destination) {
        return this.channelServices.stream()
                .filter(channelService -> channelService.support(transactionType, destination.getChannelType()))
                .findFirst()
                .orElseThrow(() -> new BizException(TpsErrorCode.UnsupportedPaymentChannel,
                        destination.getChannelType().name()));

    }

    public Optional<CounterpartyTransaction> getCounterpartyTransaction(Transaction transaction) {
        return Optional.ofNullable(transaction.getTransactionInfo())
                .map(transactionInfo -> this
                        .selectChannelService(transaction.checkTransactionType(),
                                transactionInfo.getDestination().getCounterpartyAccount())
                        .getCounterpartyTransaction(transaction));
    }

}
