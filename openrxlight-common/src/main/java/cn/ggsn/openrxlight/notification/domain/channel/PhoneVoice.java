package cn.ggsn.openrxlight.notification.domain.channel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName PhoneVoiceAccount
 * @Author zh
 * @Description 电话语音
 * @Date 2023/12/28 18:06
 * @Version 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PhoneVoice {
    private Long id; // id
    private String name; // 名称
    /**
     * 账号相关
     **/
    private String accessKeyId;
    private String accessKeySecret;
    private String endpoint;
}
