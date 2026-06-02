package cn.ggsn.openrxlight.model.device;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Getter;

@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DeviceInfo {
    private Long id;
    private Integer category;
    private Long stationId;
    private Integer status;
    private String alias;
    private String deviceTypeName;
    private Integer maxCurrent;
    private Integer maxVoltage;
    private Integer minCurrent;
    private Integer minVoltage;
    private Integer minPower;
    private Integer maxPower;
    private Integer ratedVoltage;
    private Integer ratedCurrent;
    private Integer ratedPower;
    private List<ConnectorStatusInfo> connectorStatusInfos;
}
