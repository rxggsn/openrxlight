package cn.ggsn.rxlight.ai.callback.impl;

import java.util.stream.Stream;

import cn.ggsn.openrxlight.api.OpenRxLightV2;
import cn.ggsn.openrxlight.model.chat.Callback;
import cn.ggsn.openrxlight.request.orders.ModifyReservationRequest;
import cn.ggsn.openrxlight.response.chat.ChatResponse;
import cn.ggsn.openrxlight.utils.JsonUtils;
import cn.ggsn.rxlight.ai.agent.Chatbox;
import cn.ggsn.rxlight.ai.callback.CallbackHandler;
import cn.ggsn.rxlight.ai.domain.RxLightChatMessage;

public class ModifyReservationHandler extends CallbackHandler {

    protected ModifyReservationHandler(OpenRxLightV2 openRxLightV2, Chatbox chatbox) {
        super(openRxLightV2, chatbox);
    }

    @Override
    public Stream<ChatResponse> handle(RxLightChatMessage chatMessage) throws Exception {
        Callback callback = chatMessage.getCallback();
        ModifyReservationRequest request = JsonUtils.fromJson(JsonUtils.toJson(callback.getVariables()),
                ModifyReservationRequest.class);
        this.openRxLightV2.orders().modifyReservation(request);

        return this.chatbox.completion(
                "Your reservation has been successfully modified.",
                null);
    }

    @Override
    public boolean supports(String eventType) {
        return "modify_reservation".equals(eventType);
    }

}
