package cn.ggsn.openrxlight.transaction.pay.wechat.response;

import cn.ggsn.openrxlight.transaction.pay.wechat.domain.WechatPayWebhookCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WechatNotificationResponse {
    private WechatPayWebhookCode code;
    private String message;
}
