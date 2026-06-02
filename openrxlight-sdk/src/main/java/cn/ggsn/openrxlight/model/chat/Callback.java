package cn.ggsn.openrxlight.model.chat;

import java.util.Map;

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

    public interface CallbackType {
        String CONFIRM_LOCATION = "confirm_location";
        String CONFIRM_STATION = "confirm_station";
        String CONFIRM_RESERVATION_TIME = "confirm_reservation_time";
        String CONFIRM_RESERVATION = "confirm_reservation";
        String CUSTOM_CMD = "custom_command";
        String CONFIRM_PLATE_NO = "confirm_plate_no";
    }

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
        if (callback.getVariables() != null) {
            if (this.variables == null) {
                this.variables = callback.getVariables();
            } else {
                this.variables.putAll(callback.getVariables());
            }
        }
    }
}