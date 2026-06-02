package cn.ggsn.openrxlight.notification.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zh
 * <p>
 * <p>
 * 邮件消息体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailContentModel implements ContentModel {

    /**
     * 标题
     */
    private String title;

    /**
     * 内容(可写入HTML)
     */
    private String content;

    /**
     * 邮件附件链接
     */
    private String url;

    /**
     * 内容是否为html
     */
    private Boolean html;

    /**
     * 抄送者，邮箱逗号隔开
     */
    private String ccToReceiver;

    /**
     * 密送者，邮箱逗号隔开
     */
    private String bccToReceiver;
}
