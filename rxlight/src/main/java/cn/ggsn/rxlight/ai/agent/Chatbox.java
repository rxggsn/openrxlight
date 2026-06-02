package cn.ggsn.rxlight.ai.agent;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import cn.ggsn.openrxlight.lang.Lists2;
import cn.ggsn.openrxlight.llm.openai.Models;
import cn.ggsn.openrxlight.llm.openai.OpenAI;
import cn.ggsn.openrxlight.llm.openai.request.ChatCompletionRequest;
import cn.ggsn.openrxlight.response.chat.ChatResponse;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Chatbox {
        private final String profile;
        private final Integer maxTokens;
        private final Double temperature;
        private final Double topP;
        private final Boolean stream;
        private final Boolean enableThinking;
        private final OpenAI openAI;
        private final Integer thinkingBudget;
        private final Boolean enableSearch;
        private final String model;

        @SuppressWarnings("unchecked")
        public Stream<ChatResponse> completion(String query, List<Models.ChatMessage> histories) {
                try {
                        var messages = Lists2.concat(
                                        Lists2.of(Models.ChatMessage.system(this.profile)),
                                        histories,
                                        Lists2.of(Models.ChatMessage.user(query)));
                        return this.openAI
                                        .chatCompletion(ChatCompletionRequest
                                                        .builder()
                                                        .model(this.model)
                                                        .messages(messages)
                                                        .enableThinking(this.enableThinking)
                                                        .maxTokens(this.maxTokens)
                                                        .temperature(this.temperature)
                                                        .topP(this.topP)
                                                        .stream(this.stream)
                                                        .thinkingBudget(this.thinkingBudget)
                                                        .build())
                                        .map(chunk -> {
                                                return ChatResponse.builder()
                                                                .created(chunk.getCreated())
                                                                .object(chunk.getObject())
                                                                .id(chunk.getId())
                                                                .choices(Lists2.map(chunk.getChoices(),
                                                                                choice -> ChatResponse.Choice.builder()
                                                                                                .index(choice.getIndex())
                                                                                                .delta(ChatResponse.Delta
                                                                                                                .builder()
                                                                                                                .role(choice.getMessage()
                                                                                                                                .getRole())
                                                                                                                .content(JsonNodeFactory.instance
                                                                                                                                .textNode(choice.getMessage()
                                                                                                                                                .getContent()))
                                                                                                                .build())
                                                                                                .finishReason(choice
                                                                                                                .getFinishReason())
                                                                                                .build()))
                                                                .build();
                                        });
                } catch (IOException e) {
                        throw new RuntimeException(e);
                }
        }

}
