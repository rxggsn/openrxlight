package cn.ggsn.openrxlight.model.chat.callback.event;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import cn.ggsn.openrxlight.Constants;
import cn.ggsn.openrxlight.model.station.StationInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ConfirmReservation {
    public static final String CALLBACK_TYPE = "confirm_reservation";
    private StationInfo station;
    @JsonFormat(pattern = Constants.ISO_DATE_TIME_MILLIS_PATTERN)
    private LocalDateTime startTime;
    @JsonFormat(pattern = Constants.ISO_DATE_TIME_MILLIS_PATTERN)
    private LocalDateTime endTime;
}
