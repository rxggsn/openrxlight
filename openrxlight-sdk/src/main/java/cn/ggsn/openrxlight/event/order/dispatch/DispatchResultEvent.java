package cn.ggsn.openrxlight.event.order.dispatch;

import com.fasterxml.jackson.databind.JsonNode;

import cn.ggsn.openrxlight.response.chat.ChatResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DispatchResultEvent {
    public static final String CREATE_ORDER_TYPE = "CREATE_ORDER";
    public static final String FINISH_ORDER_TYPE = "FINISH_ORDER";
    private String type;
    private JsonNode body;
    private ChatResponse response;
}
