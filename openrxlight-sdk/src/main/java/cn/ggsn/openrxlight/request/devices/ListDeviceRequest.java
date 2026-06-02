package cn.ggsn.openrxlight.request.devices;

import java.util.TreeMap;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
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
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@EqualsAndHashCode(callSuper = false)
public class ListDeviceRequest extends PageRequest {
    private Long stationId;
    private Boolean realStatus;

    @Override
    public TreeMap<String, String> getQueryParams() {
        var params = super.getQueryParams();
        params.put("real_status", Boolean.toString(this.getRealStatus()));
        return params;
    }

}
