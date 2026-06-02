package cn.ggsn.rxlight.ai.event.impl;

import java.util.List;

import cn.ggsn.openrxlight.model.chat.Callback;
import cn.ggsn.openrxlight.response.chat.ChatResponse;
import cn.ggsn.openrxlight.response.chat.ChatResponse.Choice;
import cn.ggsn.rxlight.ai.event.EventBody;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class ChatMessage extends EventBody {
    private String id;
    private String object;
    private long created;
    private String contextId;
    private List<Choice> choices;
    private Callback callback;

    public static ChatMessage fromChatResponse(ChatResponse response) {
        return ChatMessage.builder()
                .id(response.getId())
                .object(response.getObject())
                .created(response.getCreated())
                .contextId(response.getContextId())
                .choices(response.getChoices())
                .callback(response.getCallback())
                .build();
    }

    public void merge(ChatMessage other) {
        if (other == null) {
            return;
        }
        if (other.getId() != null) {
            this.setId(other.getId());
        }
        if (other.getObject() != null) {
            this.setObject(other.getObject());
        }
        if (other.getCreated() != 0) {
            this.setCreated(other.getCreated());
        }
        if (other.getContextId() != null) {
            this.setContextId(other.getContextId());
        }
        if (other.getChoices() != null && !other.getChoices().isEmpty()) {
            if (this.choices == null) {
                this.choices = other.getChoices();
            } else {
                this.choices.addAll(other.getChoices());
            }
        }
        if (other.getCallback() != null) {
            if (this.callback == null) {
                this.setCallback(other.getCallback());
            } else {
                this.callback.merge(other.getCallback());
            }
        }
    }
}