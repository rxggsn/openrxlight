package cn.ggsn.openrxlight.notification.domain.model;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zh
 *         <p>
 *         短信内容模型
 *         <p>
 *         在前端填写的时候分开，但最后处理的时候会将url拼接在content上
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SmsContentModel implements ContentModel {

    /**
     * 短信发送内容
     */
    private JsonNode params;

    /**
     * 短信发送链接
     */
    private String url;

}
