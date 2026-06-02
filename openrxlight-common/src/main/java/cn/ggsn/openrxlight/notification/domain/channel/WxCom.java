package cn.ggsn.openrxlight.notification.domain.channel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模板消息参数
 * <p>
 * 参数示例：
 * https://developers.weixin.qq.com/doc/offiaccount/Message_Management/Template_Message_Interface.html
 * * @author zh
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WxCom {

    /**
     * 账号相关
     */
    private String appId;
    private String secret;
    private String token;

}
