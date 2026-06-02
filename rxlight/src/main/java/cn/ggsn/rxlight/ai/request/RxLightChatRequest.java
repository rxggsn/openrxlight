package cn.ggsn.rxlight.ai.request;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

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
public class RxLightChatRequest implements Validate {
    @Required
    private String query;
    private List<Message> messages;
    @Required
    private String id;
    private String contextId;
    private Callback callback;
    @Required
    private String messageType;
    @Required
    private Integer appId;
    @Required
    private String location; // latitude and longitude in "latitude,longitude" format (WGS84), e.g.,
                             // "37.7749,-122.4194", required if appId is 1;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Message {
        private String role;
        private String content;
    }
}
