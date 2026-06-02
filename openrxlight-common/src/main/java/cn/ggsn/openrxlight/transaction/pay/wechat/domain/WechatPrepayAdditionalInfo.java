package cn.ggsn.openrxlight.transaction.pay.wechat.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import cn.ggsn.openrxlight.request.Required;
import cn.ggsn.openrxlight.transaction.pay.domain.TransactionAdditionalInfo;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WechatPrepayAdditionalInfo implements TransactionAdditionalInfo {
    @Required
    private String openId;
}
