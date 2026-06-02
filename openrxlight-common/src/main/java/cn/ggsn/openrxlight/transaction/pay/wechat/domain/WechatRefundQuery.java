package cn.ggsn.openrxlight.transaction.pay.wechat.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WechatRefundQuery {
    private String refundTxnId;
}
