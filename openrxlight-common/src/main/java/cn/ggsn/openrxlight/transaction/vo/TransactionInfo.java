package cn.ggsn.openrxlight.transaction.vo;

import cn.ggsn.openrxlight.transaction.domain.CounterpartyTransaction;
import cn.ggsn.openrxlight.transaction.pay.domain.Counterparty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionInfo {
    private Counterparty source;
    private Counterparty destination;
    private String description;
    @Setter
    private CounterpartyTransaction.CounterpartyTransactionInfo counterpartyTransactionInfo;

    public TransactionInfo(Counterparty source, Counterparty destination, String description) {
        this.source = source;
        this.destination = destination;
        this.description = description;
    }
}
