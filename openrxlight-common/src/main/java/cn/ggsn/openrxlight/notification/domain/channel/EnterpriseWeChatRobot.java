package cn.ggsn.openrxlight.notification.domain.channel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 企业微信 机器人 账号信息
 *
 * @author zh
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EnterpriseWeChatRobot {
    /**
     * 自定义群机器人中的 webhook
     */
    private String webhook;

}
