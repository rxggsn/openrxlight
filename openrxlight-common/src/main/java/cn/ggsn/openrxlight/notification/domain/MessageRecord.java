package cn.ggsn.openrxlight.notification.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.dialect.type.PostgreSQLUUIDJdbcType;

import cn.ggsn.openrxlight.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/**
 * 发送信息（回执和发送记录）
 * 
 * @author other
 */
@Table(name = "nty_message_record")
@Entity
@EqualsAndHashCode(callSuper = true)
public class MessageRecord extends BaseEntity {
    @jakarta.persistence.Column(name = "template_id", nullable = false)
    private Long templateId; // 消息模板Id

    @jakarta.persistence.Column(name = "content", nullable = false)
    private String content; // 信息发送的内容

    @jakarta.persistence.Column(name = "response", nullable = true)
    private String response; // 回执信息

    @jakarta.persistence.Column(name = "status", nullable = false)
    private Integer status; // 发送状态， 10.发送 20.成功 30.失败

    @jakarta.persistence.Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;

    @jakarta.persistence.Column(name = "account_id", nullable = false)
    @JdbcType(PostgreSQLUUIDJdbcType.class)
    @JdbcTypeCode(org.hibernate.type.SqlTypes.UUID)
    private UUID accountId;
}
