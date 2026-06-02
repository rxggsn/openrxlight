package cn.ggsn.rxlight.account.request.command;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Getter;

@Getter
@JsonNaming(SnakeCaseStrategy.class)
public class FeiShuCmd {
    private String appId;
    private String userId;
    private String tenantKey;
}
