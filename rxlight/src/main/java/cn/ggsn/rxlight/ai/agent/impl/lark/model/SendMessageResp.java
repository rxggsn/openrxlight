package cn.ggsn.rxlight.ai.agent.impl.lark.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SendMessageResp {
    private String cardId;
    private String messageId;

}
