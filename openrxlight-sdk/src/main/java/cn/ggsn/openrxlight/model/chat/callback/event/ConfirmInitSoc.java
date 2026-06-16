package cn.ggsn.openrxlight.model.chat.callback.event;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ConfirmInitSoc {
    public static final String CALLBACK_TYPE = "confirm_init_soc";

    private Short initialSoc;
}
