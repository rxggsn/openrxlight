package cn.ggsn.openrxlight.model.chat.callback.event;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import cn.ggsn.openrxlight.model.station.StationInfo;
import cn.ggsn.openrxlight.model.station.StationSpace;
import lombok.Data;

@Data
@JsonNaming(SnakeCaseStrategy.class)
public class ConfirmPlateNo {
    public static final String CALLBACK_TYPE = "confirm_plate_no";
    private StationInfo station;
    private StationSpace space;
}
