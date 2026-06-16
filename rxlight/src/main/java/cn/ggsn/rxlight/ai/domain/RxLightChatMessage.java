package cn.ggsn.rxlight.ai.domain;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.dialect.type.PostgreSQLJsonPGObjectJsonbType;

import com.fasterxml.jackson.annotation.JsonIgnore;

import cn.ggsn.openrxlight.lang.Lists2;
import cn.ggsn.openrxlight.lang.Maps2;
import cn.ggsn.openrxlight.model.chat.Callback;
import cn.ggsn.openrxlight.model.chat.RoleType;
import cn.ggsn.openrxlight.request.chat.UserMessageType;
import cn.ggsn.openrxlight.response.chat.ChatResponse;
import cn.ggsn.openrxlight.response.chat.ChatResponse.Attachment;
import cn.ggsn.openrxlight.response.chat.ChatResponse.Usage;
import cn.ggsn.openrxlight.utils.JsonUtils;
import cn.ggsn.rxlight.ai.event.CallbackEventType;
import cn.ggsn.openrxlight.Constants;
import cn.ggsn.openrxlight.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "chat_messages")
@EqualsAndHashCode(callSuper = true)
public class RxLightChatMessage extends BaseEntity {
    @Column(name = "user_id", nullable = false)
    private UUID userId; // User ID
    @Column(name = "context_id", nullable = true)
    private String contextId; // Context ID
    @Column(name = "app_id", nullable = false)
    private Integer appId; // Application ID
    @Column(name = "openrxlight_message_id", nullable = false)
    private String openrxlightMessageId; // OpenRxLight Message ID
    @Column(name = "app_message_id", nullable = true)
    private String appMessageId; // App Message ID
    @Column(name = "content", nullable = true)
    private String content; // Message content
    @Column(name = "callback", nullable = true)
    @JdbcType(PostgreSQLJsonPGObjectJsonbType.class)
    @JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    private Callback callback; // Callback information (JSON)
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt; // Creation timestamp
    @Column(name = "message_type", nullable = false)
    private String messageType; // Message type (e.g., "text", "image", etc.)
    @Column(name = "role", nullable = false)
    private String role;
    @Column(name = "attachments", nullable = true)
    @JdbcType(PostgreSQLJsonPGObjectJsonbType.class)
    @JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    private List<Map<String, Object>> attachments; // Attachments (if any)
    @Column(name = "extra", nullable = true)
    @JdbcType(PostgreSQLJsonPGObjectJsonbType.class)
    @JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    private ExtraInfo extra; // Extra information (if any)
    @Transient
    @JsonIgnore
    private String openrxlightAccountId; // OpenRxLight Account ID

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExtraInfo {
        private String location; // latitude and longitude in "latitude,longitude" format (WGS84), e.g.,
                                 // "37.7749,-122.4194", required if appId is 1;
        private Usage usage; // credit usage information (if any)

    }

    public static Optional<RxLightChatMessage> findByAppMessageId(Integer id, UUID accountId, String messageId) {
        return find("app_id = $1 and user_id = $2 and app_message_id = $3", id, accountId, messageId)
                .firstResultOptional();
    }

    public static RxLightChatMessage fromChatResponse(ChatResponse response) {
        return RxLightChatMessage.builder()
                .contextId(response.getContextId())
                .openrxlightMessageId(response.getId())
                .content(Optional.ofNullable(response.getContent())
                        .map(content -> JsonUtils.toJson(content))
                        .orElse(null))
                .callback(response.getCallback())
                .createdAt(LocalDateTime.now())
                .messageType(RxLightChatMessage.getMessageTypeFromObject(response.getObject()).getName())
                .role(RoleType.ASSISTANT.getName())
                .attachments(Lists2.map(response.getAttachments(), attachment -> {
                    Map<String, Object> map = Maps2.empty();
                    map.put("file_id", attachment.getFileId());
                    map.put("filename", attachment.getFilename());
                    return map;
                }))
                .extra(Optional.ofNullable(response.getUsage())
                        .map(usage -> ExtraInfo.builder()
                                .usage(usage)
                                .build())
                        .orElse(null))
                .build();
    }

    @JsonIgnore
    public List<Attachment> getAttachmentObjects() {
        if (this.attachments == null) {
            return Lists2.empty();
        }
        return Lists2.map(this.attachments, map -> Attachment.builder()
                .fileId(map.containsKey("file_id") ? (String) map.get("file_id") : null)
                .filename(map.containsKey("filename") ? (String) map.get("filename") : null)
                .file(map.containsKey("file") ? (File) map.get("file") : null)
                .build());
    }

    public boolean isPayForBill() {
        return StringUtils.equals(this.messageType, UserMessageType.CALLBACK.getName())
                && this.callback != null
                && (StringUtils.equals(this.callback.getType(), CallbackEventType.PAY_FOR_ADDED_ON)
                        || StringUtils.equals(this.callback.getType(), CallbackEventType.PAY_FOR_BILL));
    }

    public void detachAttachments() {
        Lists2.foreach(this.attachments, map -> {
            if (map.containsKey("file")) {
                map.remove("file");
            }
        });
    }

    public void setLocation(String location) {
        if (this.extra == null) {
            this.extra = new ExtraInfo();
        }
        this.extra.setLocation(location);
    }

    private static UserMessageType getMessageTypeFromObject(String object) {
        if (StringUtils.equals(object, Constants.COMPLETE_CHUNK)) {
            return UserMessageType.POST;
        } else if (StringUtils.equals(object, Constants.CHAT_CALLBACK)
                || StringUtils.equals(object, Constants.CHAT_HYBRID)) {
            return UserMessageType.CALLBACK;
        } else {
            return UserMessageType.POST;
        }
    }
}
