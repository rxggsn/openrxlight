package cn.ggsn.openrxlight.transaction.service.payment;

import cn.ggsn.openrxlight.transaction.domain.ChannelType;
import cn.ggsn.openrxlight.transaction.domain.CounterpartyTransaction;
import cn.ggsn.openrxlight.transaction.domain.Transaction;
import cn.ggsn.openrxlight.transaction.domain.TransactionType;
import cn.ggsn.openrxlight.transaction.vo.Payment;

public interface PaymentChannel {

    boolean support(TransactionType transactionType, ChannelType channelType);

    CounterpartyTransaction doPayment(Payment payment);

    CounterpartyTransaction getCounterpartyTransaction(Transaction transaction);

    int maximumRefundCount();

}
