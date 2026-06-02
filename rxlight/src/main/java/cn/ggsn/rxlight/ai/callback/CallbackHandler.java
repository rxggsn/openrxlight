package cn.ggsn.rxlight.ai.callback;

import java.util.stream.Stream;

import cn.ggsn.openrxlight.api.OpenRxLightV2;
import cn.ggsn.openrxlight.response.chat.ChatResponse;
import cn.ggsn.rxlight.ai.agent.Chatbox;
import cn.ggsn.rxlight.ai.domain.RxLightChatMessage;

public abstract class CallbackHandler {

    protected final OpenRxLightV2 openRxLightV2;
    protected final Chatbox chatbox;

    protected CallbackHandler(OpenRxLightV2 openRxLightV2, Chatbox chatbox) {
        this.openRxLightV2 = openRxLightV2;
        this.chatbox = chatbox;
    }

    abstract public Stream<ChatResponse> handle(RxLightChatMessage chatMessage) throws Exception;

    abstract public boolean supports(String eventType);

}
