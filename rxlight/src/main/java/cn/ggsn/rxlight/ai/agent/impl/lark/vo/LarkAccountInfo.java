package cn.ggsn.rxlight.ai.agent.impl.lark.vo;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import cn.ggsn.openrxlight.domain.ExternalAccount.ExternalAccountInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class LarkAccountInfo implements ExternalAccountInfo {
    private String appId;
    private String openId;
    private String unionId;
    private String userId;
    private Integer sceneType;
}
