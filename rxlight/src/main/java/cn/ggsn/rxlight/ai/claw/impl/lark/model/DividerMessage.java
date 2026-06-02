package cn.ggsn.rxlight.ai.claw.impl.lark.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DividerMessage {
    private String type;
    private DividerParams params;
    private DividerOptions options;

    public DividerMessage(String dividerText) {
        this.type = "divider";
        this.params = DividerParams.builder()
                .dividerText(DividerParams.DividerText.builder()
                        .text(dividerText)
                        .build())
                .build();
        this.options = DividerOptions.builder().needRollup(true).build();
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DividerParams {
        @JsonProperty("divider_text")
        private DividerText dividerText;

        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class DividerText {
            private String text;
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DividerOptions {
        @JsonProperty("need_rollup")
        private Boolean needRollup;
    }
}
