package cn.ggsn.openrxlight.request.orders;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetQueueInfoRequest {
    private Long stationId;
    private Long rangeId;
}
