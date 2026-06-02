package cn.ggsn.rxlight.ai.callback.impl;

import java.util.Map;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import cn.ggsn.openrxlight.api.OpenRxLightV2;
import cn.ggsn.openrxlight.model.chat.Callback;
import cn.ggsn.openrxlight.model.chat.callback.event.ConfirmReservation;
import cn.ggsn.openrxlight.model.station.StationInfo;
import cn.ggsn.openrxlight.request.orders.CreateReservationRequest;
import cn.ggsn.openrxlight.response.chat.ChatResponse;
import cn.ggsn.openrxlight.response.orders.CreateReservationResponse;
import cn.ggsn.rxlight.ai.agent.Chatbox;
import cn.ggsn.rxlight.ai.callback.CallbackHandler;
import cn.ggsn.rxlight.ai.domain.RxLightChatMessage;
import cn.ggsn.rxlight.orders.domain.RxLightReservation;

public class ConfirmReservationHandler extends CallbackHandler {

        public ConfirmReservationHandler(OpenRxLightV2 openRxLightV2, Chatbox chatbox) {
                super(openRxLightV2, chatbox);
        }

        @Override
        public Stream<ChatResponse> handle(RxLightChatMessage chatMessage) throws Exception {
                Callback callback = chatMessage.getCallback();
                ConfirmReservation event = callback.getVariablesAs(ConfirmReservation.class);
                StationInfo stationInfo = event.getStation();
                CreateReservationResponse reservation = this.openRxLightV2
                                .orders()
                                .createReservation(CreateReservationRequest.builder()
                                                .stationId(stationInfo.getId())
                                                .startTime(event.getStartTime())
                                                .endTime(event.getEndTime())
                                                .build());

                var internalReservation = RxLightReservation
                                .builder()
                                .accountId(chatMessage.getUserId())
                                .thirdPartyReservationId(Long.toString(reservation.getReservationId()))
                                .startTime(event.getStartTime())
                                .endTime(event.getEndTime())
                                .stationId(stationInfo.getId())
                                .stationName(stationInfo.getName())
                                .build();
                internalReservation.save();
                return this.chatbox.completion(
                                "Successfully process your reservation. After you arrive station, please scan the QR code to check in.",
                                null)
                                .map(msg -> {
                                        msg.setCallback(Callback.builder().type("reservation_confirmed")
                                                        .variables(Map.of("reservation_id",
                                                                        JsonNodeFactory.instance.numberNode(
                                                                                        internalReservation.id)))
                                                        .build());
                                        return msg;
                                });

        }

        @Override
        public boolean supports(String eventType) {
                return "confirm_reservation".equals(eventType);
        }
}
