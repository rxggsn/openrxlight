package cn.ggsn.openrxlight.transaction.domain;

import com.wechat.pay.java.service.refund.model.RefundNotification;

import cn.ggsn.openrxlight.domain.BaseEntity;
import cn.ggsn.openrxlight.lang.Lists2;
import cn.ggsn.openrxlight.model.Currency;
import cn.ggsn.openrxlight.transaction.vo.TransactionRefundInfo;
import cn.ggsn.openrxlight.transaction.vo.TransactionRefundInfoList;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
@Table(name = "refund_details")
@Slf4j
public class RefundDetails extends BaseEntity {
    private UUID refundTransactionId;
    private TransactionRefundInfoList refundInfos;
    private Currency refundCcy;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;

    public RefundDetails(UUID refundTransactionId, List<TransactionRefundInfo> refundInfos, Currency refundCcy) {
        this.refundTransactionId = refundTransactionId;
        this.refundInfos = new TransactionRefundInfoList(refundInfos);
        this.refundCcy = refundCcy;
        this.createdTime = LocalDateTime.now();
        this.updatedTime = LocalDateTime.now();
    }

    public void tryRefreshStatus() {

    }

    public void tryRefund(RefundNotification refund, Transaction transaction) {

    }

    public List<TransactionRefundInfo> getRefundInfosList() {
        if (this.refundInfos == null) {
            return Lists2.empty();
        }
        return this.refundInfos.getRefundInfos();
    }

}
