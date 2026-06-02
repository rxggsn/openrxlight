package cn.ggsn.openrxlight.transaction.service.payment;

import cn.ggsn.openrxlight.transaction.domain.CounterpartyTransaction;
import cn.ggsn.openrxlight.transaction.domain.Transaction;
import cn.ggsn.openrxlight.transaction.domain.TransactionType;
import cn.ggsn.openrxlight.transaction.vo.Payment;

interface PaymentSubProcessor {
    CounterpartyTransaction process(Payment payment);

    boolean support(TransactionType transactionType);

    CounterpartyTransaction getCounterpartyTransaction(Transaction transaction);
}
