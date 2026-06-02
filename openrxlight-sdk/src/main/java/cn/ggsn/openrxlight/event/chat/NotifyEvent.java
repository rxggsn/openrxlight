package cn.ggsn.openrxlight.event.chat;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import cn.ggsn.openrxlight.model.chat.Callback;
import cn.ggsn.openrxlight.response.chat.ChatResponse.Choice;
import cn.ggsn.openrxlight.utils.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy.class)
public class NotifyEvent {
    private String id;
    private List<Choice> choices;
    private String userId;
    private Integer accountType;
    private String streamId;
    private Callback callback;

    @JsonIgnore
    public JsonNode getContent() {
        if (this.choices == null || this.choices.isEmpty()) {
            return null;
        }

        this.choices.sort((c1, c2) -> c1.getIndex().compareTo(c2.getIndex()));

        return this.choices.stream()
                .filter(choice -> choice.getDelta() != null && choice.getDelta().getContent() != null)
                .map(choice -> choice.getDelta().getContent())
                .reduce(JsonNodeFactory.instance.nullNode(), (origin, newCome) -> {
                    return JsonUtils.mergeInto(newCome, origin);
                });
    }
}
