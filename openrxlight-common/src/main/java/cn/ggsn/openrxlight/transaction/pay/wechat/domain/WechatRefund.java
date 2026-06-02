package cn.ggsn.openrxlight.transaction.pay.wechat.domain;

import com.github.binarywang.wxpay.constant.WxPayConstants;

import cn.ggsn.openrxlight.utils.UUIdConverter;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

import java.util.UUID;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WechatRefund {
    private String refundId;
    private String wechatTxnId;
    private String rxDomainTxnId;
    private String channelRefundId;
    private UUID systemRefundId;
    @Setter
    private String status;
    @Setter
    private String counterpartyTraceId;

    public String getRefundTxnId() {
        if (StringUtils.isNotBlank(this.channelRefundId))
            return this.channelRefundId;
        else if (this.systemRefundId != null)
            return UUIdConverter.replaceHyphenWithEmptyChar(this.systemRefundId);
        else
            return null;
    }

    public boolean isSuccess() {
        return WxPayConstants.RefundStatus.SUCCESS.equals(this.status);
    }

    public boolean isFailed() {
        return WxPayConstants.RefundStatus.ABNORMAL.equals(this.status);
    }

    public boolean isProcessing() {
        return WxPayConstants.RefundStatus.PROCESSING.equals(this.status);
    }
}
