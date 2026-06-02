package cn.ggsn.openrxlight.transaction.pay.wechat.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import cn.ggsn.openrxlight.model.Currency;
import cn.ggsn.openrxlight.utils.UUIdConverter;
import cn.ggsn.openrxlight.transaction.pay.domain.TransactionAdditionalInfo;

import java.util.UUID;


@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WechatRefundAdditionalInfo implements TransactionAdditionalInfo {
    private UUID originTransactionId;
    private Currency originCcy;
    private String originChannelTransactionId;

    public String getOriginWechatOutTradeNo() {
        if (StringUtils.isNotBlank(this.originChannelTransactionId)) {
            return this.originChannelTransactionId;
        } else {
            return UUIdConverter.replaceHyphenWithEmptyChar(originTransactionId);
        }
    }
}
