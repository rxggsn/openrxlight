package cn.ggsn.openrxlight.model.chat.callback.event;

import java.math.BigDecimal;

import com.fasterxml.jackson.databind.EnumNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.EnumNaming;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import cn.ggsn.openrxlight.request.Required;
import cn.ggsn.openrxlight.request.Validate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(SnakeCaseStrategy.class)
public class DispatchDevice implements Validate {
    public static final String CALLBACK_TYPE = "dispatch_device";

    @EnumNaming(EnumNamingStrategies.LowerCaseStrategy.class)
    public enum DispatchJobType {
        V2V,
        V2G,
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonNaming(SnakeCaseStrategy.class)
    public static class DispatchJob {
        @Required
        private BigDecimal demand; // expect kWh demand amount
        @Required
        private Long stationId;
        @Required
        private Long spaceId;
        @Required
        private Long taskId;
        @Required
        private DispatchJobType jobType;
    }

    @Required
    private Long id;
    @Required
    private DispatchJob job;
}
