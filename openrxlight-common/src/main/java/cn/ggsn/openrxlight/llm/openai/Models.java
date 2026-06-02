package cn.ggsn.openrxlight.llm.openai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class Models {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChatMessage {
        public String role;
        public String content;

        public static ChatMessage system(String profile) {
            return new ChatMessage("system", profile);
        }

        public static ChatMessage user(String query) {
            return new ChatMessage("user", query);
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonNaming(com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ResponseFormat {
        public static final String TYPE_TEXT = "text";
        public static final String TYPE_JSON = "json_object";
        private String type;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonNaming(com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Tool {
        private String name;
        private String description;
        private JsonNode parameters;
    }

}
