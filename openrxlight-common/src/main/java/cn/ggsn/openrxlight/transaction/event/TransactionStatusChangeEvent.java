package cn.ggsn.openrxlight.transaction.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

import cn.ggsn.openrxlight.transaction.domain.TransactionStatus;
import cn.ggsn.openrxlight.transaction.domain.TransactionType;
import cn.ggsn.openrxlight.transaction.pay.domain.Counterparty;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionStatusChangeEvent {
    public static final String EVENT_TOPIC = "transaction.status.change";
    public static final String EVENT_TYPE = "TRANSACTION_STATUS_CHANGE";
    private TransactionStatus origin;
    private TransactionStatus current;
    private UUID transactionId;
    private long timestamp;
    private TransactionType transactionType;
    private Counterparty destination;
}
