package cn.ggsn.openrxlight.request.stations;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetStationSpaceRequest {
    private Long stationId;
    private Long spaceId;
}
