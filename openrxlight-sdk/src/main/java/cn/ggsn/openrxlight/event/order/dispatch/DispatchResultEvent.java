package cn.ggsn.openrxlight.event.order.dispatch;

import java.util.List;

import cn.ggsn.openrxlight.model.device.DeviceInfo;
import cn.ggsn.openrxlight.model.order.OpsOrder;
import cn.ggsn.openrxlight.model.station.StationInfo;
import cn.ggsn.openrxlight.model.station.StationSpace;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DispatchResultEvent {
    public static final String FULL_AUTO = "full_automatic";
    public static final String SEMI_AUTO = "semi_auto";
    public static final String MANUAL = "manual";
    private String type;
    private StationInfo station;
    private StationSpace space;
    private List<DeviceInfo> devices;
    private OpsOrder order;
}
