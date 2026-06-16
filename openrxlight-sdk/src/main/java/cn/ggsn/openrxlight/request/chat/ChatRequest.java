package cn.ggsn.openrxlight.request.chat;

import java.util.List;
import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.google.common.collect.Lists;

import cn.ggsn.openrxlight.model.chat.Callback;
import cn.ggsn.openrxlight.request.Required;
import cn.ggsn.openrxlight.request.Validate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ChatRequest implements Validate {
    @Required
    private String query;
    private List<Message> messages;
    @Required
    private String userId;
    @Required
    private String messageId;
    private String contextId;
    @Required
    private Integer scenario; // ONLY FOR internal use, DO NOT USE THIS FIELD IN YOUR REQUEST!!!
    @Required
    private String appId;
    private Callback callback;
    @Required
    private String messageType;
    private List<Integer> specialists; // ONLY FOR internal use, DO NOT USE THIS FIELD IN YOUR REQUEST!!!
    private String location; // latitude and longitude in "latitude,longitude" format (WGS84), e.g.,
                             // "37.7749,-122.4194", required if your app needs location info;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Message implements Validate {
        @Required
        private String role;
        @Required
        private String content;
    }

    @Override
    public void validate() {
        Validate.super.validate();
        if (UserMessageType.fromName(StringUtils.trim(this.messageType)) == null) {
            var valideTypes = Lists.newArrayList();
            for (UserMessageType type : UserMessageType.values()) {
                valideTypes.add(type.getName());
            }
            throw new IllegalArgumentException(
                    String.format("messageType must not be one of: [%s]",
                            StringUtils.join(valideTypes, ",")));
        }

        if (UserMessageType.CALLBACK.getName().equals(this.messageType)
                && this.callback == null) {
            throw new IllegalArgumentException("callback data is required for CALLBACK message type");
        }

        if (this.callback != null) {
            this.callback.validate();
        }

        if (this.messages != null) {
            for (Message message : this.messages) {
                message.validate();
            }
        }

        if (this.specialists != null && !this.specialists.isEmpty()) {
            throw new IllegalArgumentException(
                    "specialists field is for internal use only and must not be set in the request");
        }
    }
}
