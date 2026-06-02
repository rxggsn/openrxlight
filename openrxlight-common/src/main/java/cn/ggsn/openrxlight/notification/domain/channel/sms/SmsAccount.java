package cn.ggsn.openrxlight.notification.domain.channel.sms;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zh
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class SmsAccount {
    /**
     * 标识渠道商Id
     */
    protected Long id;

    /**
     * 标识渠道商名字
     */
    protected String name;

    /**
     * 【重要】类名，定位到具体的处理"下发"/"回执"逻辑
     * 依据ScriptName对应具体的某一个短信账号
     */
    protected String scriptName;

    protected Integer sceneType;

}
