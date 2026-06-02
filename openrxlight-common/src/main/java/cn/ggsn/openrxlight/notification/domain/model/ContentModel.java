package cn.ggsn.openrxlight.notification.domain.model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * 发送内容的模型
 * (不同的渠道会有不同的消息体)
 *
 * @author zh
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS)
public interface ContentModel {
}
