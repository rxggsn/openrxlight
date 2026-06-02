package cn.ggsn.openrxlight.transaction.pay.wechat.webhook;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Headers {
    private String timestamp;
    private String signature;
    private String nonce;
    private String serialNo;
    private String signType;
}
