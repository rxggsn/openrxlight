package cn.ggsn.rxlight.ai.domain.credentials;

import cn.ggsn.rxlight.ai.domain.AgentApp.Credential;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LarkCredential implements Credential {
    private String verificationToken;
    private String encryptKey;
}
