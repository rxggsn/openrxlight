package cn.ggsn.openrxlight.transaction.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

import cn.ggsn.openrxlight.model.Currency;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionRefundInfo {
    private UUID transactionId;
    private Currency refundCcy;
}
