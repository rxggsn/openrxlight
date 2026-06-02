package cn.ggsn.openrxlight.llm.openai.request;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import cn.ggsn.openrxlight.llm.openai.Models;
import cn.ggsn.openrxlight.llm.openai.Models.Tool;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ChatCompletionRequest {
    public String model;
    public List<Models.ChatMessage> messages;
    private Integer maxTokens;
    private Double temperature;
    private Double topP;
    private Boolean stream;
    private Map<String, Object> streamOptions;
    private Models.ResponseFormat responseFormat;
    private Boolean enableThinking;
    private Integer thinkingBudget;
    private Map<String, Object> extraBody;
    private Integer seed;
    private List<String> stop;
    private List<Tool> tools;
    private String toolChoice;
    private Boolean parallelToolCalls;
}