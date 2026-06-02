package cn.ggsn.openrxlight.transaction.pay.wechat.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WechatPayOrderQuery {
    private String transactionId;
}
