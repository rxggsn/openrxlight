package cn.ggsn.rxlight.orders.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import cn.ggsn.openrxlight.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "reservation")
@Entity
public class RxLightReservation extends BaseEntity {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String thirdPartyReservationId;
    private String stationName;
    private Long stationId;
    private UUID accountId;
}
