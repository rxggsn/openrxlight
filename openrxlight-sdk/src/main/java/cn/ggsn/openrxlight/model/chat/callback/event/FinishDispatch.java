package cn.ggsn.openrxlight.model.chat.callback.event;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import cn.ggsn.openrxlight.request.Validate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(SnakeCaseStrategy.class)
public class FinishDispatch implements Validate {
    public static final String CALLBACK_TYPE = "finish_dispatch";
    private Long dispatchId;
    private Boolean success;
}
