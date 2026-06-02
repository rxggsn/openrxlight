package cn.ggsn.openrxlight.transaction.vo;

import lombok.*;
import cn.ggsn.openrxlight.request.Required;
import cn.ggsn.openrxlight.request.Validate;
import cn.ggsn.openrxlight.transaction.domain.Transaction;
import cn.ggsn.openrxlight.transaction.pay.domain.TransactionAdditionalInfo;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment implements Validate {
    @Required
    private TransactionAdditionalInfo transactionAdditionalInfo;
    @Required
    private Transaction transaction;

    public String getRxLightTransactionIdInCounterparty() {
        return this.transaction.getRxLightOrderIdInCounterparty();
    }
}
