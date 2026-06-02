package cn.ggsn.openrxlight.notification.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName PhoneVoiceContentModel
 * @Author zh
 * @Description 电话语音模块
 * @Date 2023/12/28 17:53
 * @Version 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PhoneVoiceContentModel implements ContentModel {
    /**
     * 电话语音发送内容
     */
    private String content;

    private String ttsCode;

    private Integer playTimes;

    private Integer speed;
}
