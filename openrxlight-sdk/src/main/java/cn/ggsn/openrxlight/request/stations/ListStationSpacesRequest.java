package cn.ggsn.openrxlight.request.stations;

import cn.ggsn.openrxlight.request.PageRequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class ListStationSpacesRequest extends PageRequest {
    private Long stationId;
}
