package cn.ggsn.openrxlight.request.devices;

import java.util.TreeMap;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class GetDeviceRequest {
    private Long deviceId;
    private Boolean realStatus;

    public TreeMap<String, String> getQueryParams() {
        TreeMap<String, String> params = new TreeMap<>();
        params.put("real_status", Boolean.toString(this.realStatus));
        return params;
    }
}
