package cn.ggsn.rxlight.ai.agent.impl.lark.card;

import java.util.Map;
import java.util.function.Function;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.node.ArrayNode;
import cn.ggsn.openrxlight.lang.Maps2;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(SnakeCaseStrategy.class)
public class CardTemplate {
    private String templateId;
    private String streamKey;
    private JsonNode content;

    public void replaceVariables(Map<String, JsonNode> variables, Function<JsonNode, JsonNode> preProcess) {
        if (Maps2.isEmpty(variables)) {
            return;
        }
        variables.forEach((key, value) -> {
            replaceVariable(this.content, content, key, value, preProcess);
        });
    }

    private void replaceVariable(JsonNode parent, JsonNode origin, String key, JsonNode value,
            Function<JsonNode, JsonNode> preProcess) {
        if (origin.isObject()) {
            origin.fieldNames().forEachRemaining(fieldName -> {
                JsonNode child = origin.get(fieldName);
                if (child.isTextual()) {
                    if (StringUtils.equals(child.asText(), String.format("${%s}", key))) {
                        ((com.fasterxml.jackson.databind.node.ObjectNode) origin).set(fieldName,
                                preProcess != null ? preProcess.apply(value) : value);
                    } else if (StringUtils.contains(child.asText(), String.format("${%s}", key))) {
                        var afterPreprocess = preProcess != null ? preProcess.apply(value) : value;
                        ((com.fasterxml.jackson.databind.node.ObjectNode) origin).put(fieldName,
                                StringUtils.replace(child.asText(), String.format("${%s}", key),
                                        afterPreprocess.asText()));
                    }
                } else {
                    replaceVariable(origin, child, key, value, preProcess);
                }
            });
        } else if (origin.isArray()) {
            for (int i = 0; i < origin.size(); i++) {
                var item = origin.get(i);
                if (item.isTextual()) {
                    if (StringUtils.equals(item.asText(), String.format("\"${%s}\"", key))) {
                        ((ArrayNode) origin).set(i, preProcess != null ? preProcess.apply(value) : value);
                    } else if (StringUtils.contains(item.asText(), String.format("${%s}", key))) {
                        var afterPreprocess = preProcess != null ? preProcess.apply(value) : value;
                        ((ArrayNode) origin).set(i,
                                StringUtils.replace(item.asText(), String.format("${%s}", key),
                                        afterPreprocess.asText()));
                    }
                } else {
                    replaceVariable(origin, item, key, value, preProcess);
                }
            }
        }
    }

    public void mergeElements(CardTemplate other) {
        if (other != null) {
            var elements = ((ArrayNode) other.content);
            var selfElements = ((ArrayNode) this.content.get("body").get("elements"));
            selfElements.iterator().forEachRemaining(element -> {
                if (StringUtils.equals(element.get("tag").asText(), "form")) {
                    ArrayNode formElements = (ArrayNode) element.get("elements");
                    if (formElements.isEmpty()) {
                        formElements.addAll(elements);
                    } else {
                        elements.forEach(e -> formElements.insert(formElements.size() - 1, e));
                    }
                }
            });
        }
    }
}
