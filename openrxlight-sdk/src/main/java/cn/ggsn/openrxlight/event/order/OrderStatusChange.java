package cn.ggsn.openrxlight.event.order;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusChange {
    private String orderNo;
    private Short status;
    private LocalDateTime chargeStartTime;
    private LocalDateTime chargeEndTime;
    private String merchantOrderNo;
}
