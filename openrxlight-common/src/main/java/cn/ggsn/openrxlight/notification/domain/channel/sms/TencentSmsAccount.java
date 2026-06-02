package cn.ggsn.openrxlight.notification.domain.channel.sms;

import cn.ggsn.openrxlight.notification.domain.ChannelConfiguration;
import cn.ggsn.openrxlight.notification.domain.NtySceneType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 腾讯短信参数
 * <p>
 * 账号参数示例：
 * {
 * "url": "sms.tencentcloudapi.com",
 * "region": "ap-guangzhou",
 * "secretId": "AKIDhDxxxxxxxx1WljQq",
 * "secretKey": "B4hwww39yxxxrrrrgxyi",
 * "smsSdkAppId": "1423123125",
 * "templateId": "1182097",
 * "signName": "ggsn公众号",
 * "supplierId": 10,
 * "supplierName": "腾讯云",
 * "scriptName": "TencentSmsScript"
 * }
 *
 * @author zh
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TencentSmsAccount extends SmsAccount implements ChannelConfiguration.ChannelAccount {

    /**
     * api相关
     */
    private String url;
    private String region;

    /**
     * 账号相关
     */
    private String secretId;
    private String secretKey;
    private String smsSdkAppId;
    private String templateId;
    private String signName;

    @Override
    public boolean supports(NtySceneType sceneType) {
        return sceneType.getCode() == this.sceneType;
    }
}
