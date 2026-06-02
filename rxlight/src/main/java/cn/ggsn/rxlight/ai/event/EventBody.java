package cn.ggsn.rxlight.ai.event;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public abstract class EventBody {
    private UserId userId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserId {
        private String unionId;
        private String openId;
    }
}
