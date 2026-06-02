package cn.ggsn.openrxlight.request.stations;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetStationInfoRequest {
    private Long stationId;
}
