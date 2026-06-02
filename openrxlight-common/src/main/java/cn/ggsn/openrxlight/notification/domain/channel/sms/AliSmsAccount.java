package cn.ggsn.openrxlight.notification.domain.channel.sms;

import cn.ggsn.openrxlight.notification.domain.ChannelConfiguration;
import cn.ggsn.openrxlight.notification.domain.NtySceneType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @ClassName AliSmsAccount
 * @Author zh
 * @Description 阿里云短信
 * @Date 2023/5/7 11:39
 * @Version 1.0
 *          “url”：“dysmsapi.aliyuncs.com”
 *          “region”：“cn-hangzhou”
 *          “accessKeyId”：”accessKeyId“
 *          “accessKeySecret”：“accessKeySecret”
 *          “SignName”：“黑马原力”
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AliSmsAccount extends SmsAccount implements ChannelConfiguration.ChannelAccount {
    /**
     * api相关
     */
    private String endpoint;
    private String regionId;

    /**
     * 账号相关
     **/
    private String accessKeyId;
    private String accessKeySecret;
    private String signName;

    private String templateCode;

    private Long supplierId;
    private String supplierName;

    @Override
    public boolean supports(NtySceneType sceneType) {
        return sceneType.getCode() == this.sceneType;
    }
}
