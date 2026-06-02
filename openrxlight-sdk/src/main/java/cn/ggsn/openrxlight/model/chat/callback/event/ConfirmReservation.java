package cn.ggsn.openrxlight.model.chat.callback.event;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

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
    private StationInfo station;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
