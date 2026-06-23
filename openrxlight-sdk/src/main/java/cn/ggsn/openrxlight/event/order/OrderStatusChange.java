package cn.ggsn.openrxlight.event.order;

import java.time.LocalDateTime;

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
public class OrderStatusChange {
    private String orderNo;
    private Short status;
    private LocalDateTime chargeStartTime;
    private LocalDateTime chargeEndTime;
    private String merchantOrderNo;
}
