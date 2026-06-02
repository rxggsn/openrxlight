package cn.ggsn.openrxlight.transaction.pay.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

import cn.ggsn.openrxlight.request.Required;
import cn.ggsn.openrxlight.request.Validate;

@Getter
@NoArgsConstructor
public class Counterparty implements Validate {
    @Required
    private UUID counterpartyId;
    @Required
    private CounterpartyAccount counterpartyAccount;

    public Counterparty(UUID counterpartyId, CounterpartyAccount counterpartyAccount) {
        this.counterpartyId = counterpartyId;
        this.counterpartyAccount = counterpartyAccount;
    }
}
