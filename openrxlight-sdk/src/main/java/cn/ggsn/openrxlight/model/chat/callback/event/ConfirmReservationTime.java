package cn.ggsn.openrxlight.model.chat.callback.event;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import cn.ggsn.openrxlight.utils.DateTimeUtils;
import lombok.Data;

@Data
@JsonNaming(SnakeCaseStrategy.class)
public class ConfirmReservationTime {
    public static final String CALLBACK_TYPE = "confirm_reservation_time";
    private String reservationTime;

    @JsonIgnore
    public LocalDateTime getNativeReservationTime() {
        return DateTimeUtils.parse(this.reservationTime);
    }
}
