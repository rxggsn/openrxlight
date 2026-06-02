package cn.ggsn.openrxlight.notification.domain.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zh
 * <p>
 * 通知栏消息推送
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PushContentModel implements ContentModel {

    private String title;
    private String content;
    private String url;
}
