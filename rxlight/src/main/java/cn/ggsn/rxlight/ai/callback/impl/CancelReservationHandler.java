package cn.ggsn.rxlight.ai.callback.impl;

import java.util.stream.Stream;

import cn.ggsn.openrxlight.api.OpenRxLightV2;
import cn.ggsn.openrxlight.response.chat.ChatResponse;
import cn.ggsn.rxlight.ai.agent.Chatbox;
import cn.ggsn.rxlight.ai.callback.CallbackHandler;
import cn.ggsn.rxlight.ai.domain.RxLightChatMessage;

public class CancelReservationHandler extends CallbackHandler {

    public CancelReservationHandler(OpenRxLightV2 openRxLightV2, Chatbox chatbox) {
        super(openRxLightV2, chatbox);
    }

    @Override
    public boolean supports(String eventType) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'supports'");
    }

    @Override
    public Stream<ChatResponse> handle(RxLightChatMessage chatMessage) throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handle'");
    }

}
