package cn.ggsn.openrxlight.transaction.pay.domain;

import lombok.Data;

import java.util.List;

import cn.ggsn.openrxlight.transaction.domain.TransactionType;

@Data
public abstract class PayClientBaseConfig {
    private List<TransactionType> supportTransactionTypes;
    private Boolean enabled;
}
