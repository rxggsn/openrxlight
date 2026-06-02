package cn.ggsn.rxlight.ai.request;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import cn.ggsn.openrxlight.api.OpenRxLightBuilder;
import cn.ggsn.openrxlight.request.Required;
import cn.ggsn.openrxlight.request.Validate;
import cn.ggsn.openrxlight.web.DispatchCommand;
import cn.ggsn.rxlight.ai.domain.AgentApp;
import cn.ggsn.rxlight.ai.domain.AppType;
import cn.ggsn.rxlight.ai.domain.credentials.LarkCredential;
import cn.ggsn.rxlight.ai.domain.credentials.OpenRxLightCredential;
import lombok.Getter;

@Getter
@JsonNaming(com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CreateAgentAppRequest extends DispatchCommand<AgentApp> implements Validate {
    @Getter
    @JsonNaming(com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class CreateAppTypeAgentRequest extends CreateAgentAppRequest {
        private OpenRxLightBuilder openrxlight;

        @Override
        public AgentApp toResource() {
            return AgentApp.builder()
                    .appId(this.getAppId())
                    .appSecret(this.getAppSecret())
                    .appType(AppType.APP.getCode())
                    .openrxlight(OpenRxLightCredential.builder()
                            .inner(this.openrxlight)
                            .build())
                    .build();
        }
    }

    @Getter
    @JsonNaming(com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class CreateFeishuAgentRequest extends CreateAgentAppRequest {
        private String encryptKey;
        private String verificationToken;

        @Override
        public AgentApp toResource() {
            return AgentApp.builder()
                    .appId(this.getAppId())
                    .appSecret(this.getAppSecret())
                    .appType(AppType.FEISHU.getCode())
                    .credential(LarkCredential.builder()
                            .encryptKey(this.encryptKey)
                            .verificationToken(this.verificationToken)
                            .build())
                    .build();
        }
    }

    static {
        register(AppType.FEISHU.getCode(), CreateFeishuAgentRequest.class);
        register(AppType.APP.getCode(), CreateAppTypeAgentRequest.class);
    }

    @Required
    private String appId;
    @Required
    private String appSecret;
    @Required
    private Integer scenario;
}
