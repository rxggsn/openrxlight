package cn.ggsn.openrxlight.model.chat.callback.event;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;

@Data
@JsonNaming(SnakeCaseStrategy.class)
public class ConfirmReservationTime {
    private LocalDateTime reservationTime;
}
