package cn.ggsn.openrxlight.model.chat;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import cn.ggsn.openrxlight.request.Required;
import cn.ggsn.openrxlight.request.Validate;
import cn.ggsn.openrxlight.utils.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = { "type", "callbackId" })
public class Callback implements Validate, Cloneable {

    @Required
    private String type;
    @Required
    private Map<String, JsonNode> variables;
    private String callbackId;

    public <T> T getVariablesAs(Class<T> class1) {
        return JsonUtils.fromJson(JsonUtils.toJson(this.variables), class1);
    }

    public void merge(Callback callback) {
        if (callback == null) {
            return;
        }
        if (StringUtils.isBlank(this.type)) {
            this.type = callback.type;
        }
        if (callback.getVariables() != null) {
            if (this.variables == null) {
                this.variables = callback.getVariables();
            } else {
                this.variables.putAll(callback.getVariables());
            }
        }
        if (StringUtils.isBlank(this.callbackId)) {
            this.callbackId = callback.callbackId;
        }
    }

    @Override
    public Callback clone() {
        Map<String, JsonNode> clonedVariables = null;
        if (this.variables != null) {
            clonedVariables = new HashMap<>();
            for (Map.Entry<String, JsonNode> entry : this.variables.entrySet()) {
                clonedVariables.put(entry.getKey(), entry.getValue().deepCopy());
            }
        }
        return new Callback(this.type, clonedVariables, this.callbackId);
    }
}