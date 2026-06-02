package cn.ggsn.openrxlight.model.device;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Getter;

@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ConnectorStatusInfo {
    private String connectorId;
    private Integer type;
    private Integer status;
    private Integer current;
    private Integer voltage;
    private Integer power;
    private Integer aPhaseCurrent;
    private Integer bPhaseCurrent;
    private Integer cPhaseCurrent;
    private Integer aPhaseVoltage;
    private Integer bPhaseVoltage;
    private Integer cPhaseVoltage;
    private Integer aPhasePower;
    private Integer bPhasePower;
    private Integer cPhasePower;
}