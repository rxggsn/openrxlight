package cn.ggsn.rxlight.ai.claw.impl.lark.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardMessage {
    private String type;
    private CardData data;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CardData {
        @JsonProperty("card_id")
        private String cardId;
        @JsonProperty("template_id")
        private String templateId;
    }
}
