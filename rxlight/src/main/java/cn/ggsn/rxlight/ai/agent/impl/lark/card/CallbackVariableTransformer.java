package cn.ggsn.rxlight.ai.agent.impl.lark.card;

import java.math.RoundingMode;
import java.util.Map;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.TextNode;

import cn.ggsn.openrxlight.Constants;
import cn.ggsn.openrxlight.lang.Lists2;
import cn.ggsn.openrxlight.model.chat.Callback;
import cn.ggsn.openrxlight.model.chat.callback.event.ConfirmOrder;
import cn.ggsn.openrxlight.model.chat.callback.event.ConfirmPlateNo;
import cn.ggsn.openrxlight.model.chat.callback.event.ConfirmReservation;
import cn.ggsn.openrxlight.model.chat.callback.event.ConfirmReservationTime;
import cn.ggsn.openrxlight.model.chat.callback.event.ConfirmStation;
import cn.ggsn.rxlight.ai.agent.impl.lark.vo.OptionItem;

public class CallbackVariableTransformer {
        public static Callback transform(Callback callback) {
                Callback newCallback = callback;
                switch (callback.getType()) {
                        case ConfirmStation.CALLBACK_TYPE:
                                ConfirmStation confirmStation = callback.getVariablesAs(ConfirmStation.class);
                                newCallback = transformConfirmStation(confirmStation);
                                break;
                        case ConfirmPlateNo.CALLBACK_TYPE:
                                ConfirmPlateNo confirmPlateNo = callback.getVariablesAs(ConfirmPlateNo.class);
                                newCallback = transformConfirmPlateNo(confirmPlateNo);
                                break;
                        case ConfirmReservation.CALLBACK_TYPE:
                                ConfirmReservation confirmReservation = callback
                                                .getVariablesAs(ConfirmReservation.class);
                                newCallback = transformConfirmReservation(confirmReservation);
                                break;
                        case ConfirmOrder.CALLBACK_TYPE:
                                ConfirmOrder confirmOrder = callback.getVariablesAs(ConfirmOrder.class);
                                newCallback = transformConfirmOrder(confirmOrder);
                        case ConfirmReservationTime.CALLBACK_TYPE:
                                ConfirmReservationTime confirmReservationTime = callback
                                                .getVariablesAs(ConfirmReservationTime.class);
                                newCallback = transformConfirmReservationTime(confirmReservationTime);
                                break;
                        default:
                                break;
                }
                newCallback.setCallbackId(callback.getCallbackId());
                return newCallback;
        }

        private static Callback transformConfirmReservationTime(ConfirmReservationTime confirmReservationTime) {
                return Callback.builder()
                                .type(ConfirmReservationTime.CALLBACK_TYPE)
                                .variables(Map.of(
                                                "reservation_time",
                                                JsonNodeFactory.instance.textNode(
                                                                confirmReservationTime.getNativeReservationTime().format(
                                                                                Constants.DATE_HH_MM_FORMATTER))))
                                .build();
        }

        private static Callback transformConfirmOrder(ConfirmOrder confirmOrder) {
                ArrayNode stations = JsonNodeFactory.instance.arrayNode();
                stations.add(OptionItem
                                .builder()
                                .text(OptionItem.Text.plainText(confirmOrder.getStation().getName()))
                                .value(JsonNodeFactory.instance
                                                .numberNode(confirmOrder.getStation().getId()))
                                .build()
                                .toJsonNode());
                ArrayNode spaces = JsonNodeFactory.instance.arrayNode();
                spaces.add(OptionItem
                                .builder()
                                .text(OptionItem.Text.plainText(confirmOrder.getSpace().getSpaceNo()))
                                .value(JsonNodeFactory.instance
                                                .numberNode(confirmOrder.getSpace().getId()))
                                .build()
                                .toJsonNode());
                TextNode chargeBasic = JsonNodeFactory.instance.textNode(confirmOrder.getChargeBasicDesc());
                TextNode prepay = JsonNodeFactory.instance.textNode(
                                confirmOrder.getPrepay().setScale(2, RoundingMode.HALF_UP).toEngineeringString());
                return Callback.builder()
                                .type(ConfirmOrder.CALLBACK_TYPE)
                                .variables(Map.of(
                                                "stations", stations,
                                                "spaces", spaces,
                                                "charge_basic", chargeBasic,
                                                "prepay", prepay,
                                                "plate_no",
                                                JsonNodeFactory.instance
                                                                .textNode(confirmOrder.getPlateNo())))
                                .build();
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
                                .type(ConfirmReservation.CALLBACK_TYPE)
                                .variables(Map.of(
                                                "stations", stations,
                                                "start_time",
                                                JsonNodeFactory.instance
                                                                .textNode(confirmReservation.getStartTime()
                                                                                .format(Constants.DATE_TIME_FORMATTER)),
                                                "end_time",
                                                JsonNodeFactory.instance
                                                                .textNode(confirmReservation.getEndTime().format(
                                                                                Constants.DATE_TIME_FORMATTER))))
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
                                .type(ConfirmPlateNo.CALLBACK_TYPE)
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
                                .type(ConfirmStation.CALLBACK_TYPE)
                                .variables(Map.of("stations", stationsNode))
                                .build();
        }
}
