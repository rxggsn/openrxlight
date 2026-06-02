package cn.ggsn.rxlight.ai.claw.impl.lark.card;

import java.util.Map;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import cn.ggsn.openrxlight.Constants;
import cn.ggsn.openrxlight.lang.Lists2;
import cn.ggsn.openrxlight.model.chat.Callback;
import cn.ggsn.openrxlight.model.chat.Callback.CallbackType;
import cn.ggsn.openrxlight.model.chat.callback.event.ConfirmPlateNo;
import cn.ggsn.openrxlight.model.chat.callback.event.ConfirmReservation;
import cn.ggsn.openrxlight.model.chat.callback.event.ConfirmStation;
import cn.ggsn.rxlight.ai.claw.impl.lark.vo.OptionItem;

public class CallbackVariableTransformer {
    public static Callback transform(Callback callback) {
        Callback newCallback = callback;
        switch (callback.getType()) {
            case CallbackType.CONFIRM_STATION:
                ConfirmStation confirmStation = callback.getVariablesAs(ConfirmStation.class);
                newCallback = transformConfirmStation(confirmStation);
                break;
            case CallbackType.CONFIRM_PLATE_NO:
                ConfirmPlateNo confirmPlateNo = callback.getVariablesAs(ConfirmPlateNo.class);
                newCallback = transformConfirmPlateNo(confirmPlateNo);
                break;
            case CallbackType.CONFIRM_RESERVATION:
                ConfirmReservation confirmReservation = callback.getVariablesAs(ConfirmReservation.class);
                newCallback = transformConfirmReservation(confirmReservation);
                break;
            default:
                break;
        }
        newCallback.setCallbackId(callback.getCallbackId());
        return newCallback;
    }

    private static Callback transformConfirmReservation(ConfirmReservation confirmReservation) {
        ArrayNode stations = JsonNodeFactory.instance.arrayNode();
        stations.add(OptionItem
                .builder()
                .text(OptionItem.Text.plainText(confirmReservation.getStation().getName()))
                .value(JsonNodeFactory.instance
                        .numberNode(confirmReservation.getStation().getId()))
                .build()
                .toJsonNode());
        return Callback.builder()
                .type(CallbackType.CONFIRM_RESERVATION)
                .variables(Map.of(
                        "stations", stations,
                        "start_time",
                        JsonNodeFactory.instance
                                .textNode(confirmReservation.getStartTime().format(Constants.DATE_TIME_FORMATTER)),
                        "end_time",
                        JsonNodeFactory.instance
                                .textNode(confirmReservation.getEndTime().format(Constants.DATE_TIME_FORMATTER))))
                .build();
    }

    private static Callback transformConfirmPlateNo(ConfirmPlateNo confirmPlateNo) {
        ArrayNode stations = JsonNodeFactory.instance.arrayNode();
        stations.add(OptionItem
                .builder()
                .text(OptionItem.Text.plainText(confirmPlateNo.getStation().getName()))
                .value(JsonNodeFactory.instance
                        .numberNode(confirmPlateNo.getStation().getId()))
                .build()
                .toJsonNode());
        ArrayNode spaces = JsonNodeFactory.instance.arrayNode();
        spaces.add(OptionItem
                .builder()
                .text(OptionItem.Text.plainText(confirmPlateNo.getSpace().getSpaceNo()))
                .value(JsonNodeFactory.instance
                        .numberNode(confirmPlateNo.getSpace().getId()))
                .build()
                .toJsonNode());

        return Callback.builder()
                .type(CallbackType.CONFIRM_PLATE_NO)
                .variables(Map.of("stations", stations, "spaces", spaces))
                .build();
    }

    private static Callback transformConfirmStation(ConfirmStation confirmStation) {
        ArrayNode stationsNode = JsonNodeFactory.instance.arrayNode();
        Lists2.map(confirmStation.getStations(), st -> OptionItem.builder()
                .text(OptionItem.Text.builder()
                        .text(st.getName())
                        .type(OptionItem.Text.TextType.PlainText)
                        .build())
                .value(JsonNodeFactory.instance.numberNode(st.getId()))
                .build()
                .toJsonNode())
                .forEach(stationsNode::add);
        return Callback.builder()
                .type(CallbackType.CONFIRM_STATION)
                .variables(Map.of("stations", stationsNode))
                .build();
    }
}
