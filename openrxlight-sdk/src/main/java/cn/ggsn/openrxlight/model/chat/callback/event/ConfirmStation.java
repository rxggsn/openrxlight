package cn.ggsn.openrxlight.model.chat.callback.event;

import java.util.List;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import cn.ggsn.openrxlight.model.station.StationInfo;
import lombok.Data;

@Data
@JsonNaming(SnakeCaseStrategy.class)
public class ConfirmStation {
    private List<StationInfo> stations;
}
