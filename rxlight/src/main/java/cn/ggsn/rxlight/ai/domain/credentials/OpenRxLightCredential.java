package cn.ggsn.rxlight.ai.domain.credentials;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import cn.ggsn.openrxlight.api.OpenRxLightBuilder;
import cn.ggsn.rxlight.ai.domain.AgentApp.Credential;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonNaming(SnakeCaseStrategy.class)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpenRxLightCredential implements Credential {
    @JsonUnwrapped
    private OpenRxLightBuilder inner;
}
