package cn.ggsn.openrxlight.model.chat;

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
import lombok.NoArgsConstructor;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Callback implements Validate {

    @Required
    private String type;
    @Required
    private Map<String, JsonNode> variables;
    @Required
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
    }
}