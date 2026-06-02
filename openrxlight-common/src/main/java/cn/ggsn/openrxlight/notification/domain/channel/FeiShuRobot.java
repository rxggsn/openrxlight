package cn.ggsn.openrxlight.notification.domain.channel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 飞书 机器人 账号信息
 *
 * @author zh
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FeiShuRobot {
    private Long id; // id
    private String name; // 名称
    private String webhook; // 自定义群机器人中的 webhook
    private Integer internal; // 是否是内部系统,0-否，1-是

}
