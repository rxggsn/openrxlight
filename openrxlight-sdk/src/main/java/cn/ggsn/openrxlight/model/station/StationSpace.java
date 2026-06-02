package cn.ggsn.openrxlight.model.station;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Getter;

@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class StationSpace {
    private Long id;
    private String spaceNo;
    private Long stationId;
    private Long rangeId;
    private Boolean enable;
    private QueueInfo queueInfo;
}
