package cn.ggsn.openrxlight.llm.openai.response;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import cn.ggsn.openrxlight.lang.Lists2;
import lombok.Data;

import java.util.List;

@Data
@JsonNaming(com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ChoiceChunk {
    private String id;
    private String object;
    private Long created;
    private String model;
    private String systemFingerprint;
    private List<Choice> choices;
    private Usage usage;

    @Data
    @JsonNaming(com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Choice {
        private Integer index;
        private String finishReason;
        private Object logprobs;
        private Message message;
    }

    @Data
    @JsonNaming(com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Message {
        private String role;
        private String content;
        private List<ToolCall> toolCalls;
    }

    @Data
    @JsonNaming(com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ToolCall {
        private String id;
        private String type;
        private Function function;
        private Integer index;

        @Data
        @JsonNaming(com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class Function {
            private String name;
            private String arguments;
        }
    }

    @Data
    @JsonNaming(com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Usage {
        private Integer promptTokens;
        private Integer completionTokens;
        private Integer totalTokens;
        private PromptTokensDetails promptTokensDetails;
    }

    @Data
    @JsonNaming(com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class PromptTokensDetails {
        private Integer cachedTokens;
    }

    public void merge(ChoiceChunk item) {
        if (item.getChoices() != null) {
            if (this.choices == null) {
                this.choices = item.getChoices();
            } else {
                this.choices.addAll(item.getChoices());
            }
        }
        if (item.getUsage() != null) {
            if (this.usage == null) {
                this.usage = item.getUsage();
            } else {
                this.usage.setPromptTokens(this.usage.getPromptTokens() + item.getUsage().getPromptTokens());
                this.usage
                        .setCompletionTokens(this.usage.getCompletionTokens() + item.getUsage().getCompletionTokens());
                this.usage.setTotalTokens(this.usage.getTotalTokens() + item.getUsage().getTotalTokens());
                if (this.usage.getPromptTokensDetails() != null && item.getUsage().getPromptTokensDetails() != null) {
                    this.usage.getPromptTokensDetails()
                            .setCachedTokens(this.usage.getPromptTokensDetails().getCachedTokens()
                                    + item.getUsage().getPromptTokensDetails().getCachedTokens());
                }
            }
        }

        this.id = item.getId();
        this.object = item.getObject();
        this.created = item.getCreated();
        this.model = item.getModel();
        this.systemFingerprint = item.getSystemFingerprint();
    }

    public List<ToolCall> getToolCalls() {
        if (this.choices == null) {
            return List.of();
        }
        return Lists2.flatMap(this.choices,
                choice -> choice.getMessage() != null ? choice.getMessage().getToolCalls() : Lists2.empty());
    }
}