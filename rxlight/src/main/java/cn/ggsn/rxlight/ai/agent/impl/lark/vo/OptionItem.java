package cn.ggsn.rxlight.ai.agent.impl.lark.vo;

import com.fasterxml.jackson.databind.EnumNamingStrategies;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.EnumNaming;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import cn.ggsn.openrxlight.utils.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class OptionItem {
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Text {
        private String content;
        private TextType tag;

        @EnumNaming(EnumNamingStrategies.SnakeCaseStrategy.class)
        public enum TextType {
            PLAIN_TEXT,
        }

        public static Text plainText(String name) {
            return Text.builder()
                    .content(name)
                    .tag(TextType.PLAIN_TEXT)
                    .build();
        }
    }

    private Text text;
    private JsonNode value;

    public JsonNode toJsonNode() {
        return JsonUtils.toJsonNode(this);
    }
}
