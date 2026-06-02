package cn.ggsn.openrxlight.model.chat.callback.event;

import java.util.List;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import cn.ggsn.openrxlight.model.station.StationInfo;
import cn.ggsn.openrxlight.model.station.StationSpace;
import lombok.Data;

@Data
@JsonNaming(SnakeCaseStrategy.class)
public class ConfirmSpaceNo {
    private StationInfo station;
    private List<StationSpace> spaces;
}
