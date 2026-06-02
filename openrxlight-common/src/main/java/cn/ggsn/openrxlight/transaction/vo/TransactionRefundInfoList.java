package cn.ggsn.openrxlight.transaction.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRefundInfoList {
    private List<TransactionRefundInfo> refundInfos;
}
