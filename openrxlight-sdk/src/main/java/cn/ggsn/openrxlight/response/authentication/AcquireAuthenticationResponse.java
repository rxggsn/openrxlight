package cn.ggsn.openrxlight.response.authentication;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Getter;

@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AcquireAuthenticationResponse {
    private String accessToken;
    private int expiredIn;
}
