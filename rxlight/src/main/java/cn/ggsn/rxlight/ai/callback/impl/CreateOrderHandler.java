package cn.ggsn.rxlight.ai.callback.impl;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import cn.ggsn.openrxlight.account.domain.Account;
import cn.ggsn.openrxlight.account.error.AccountError;
import cn.ggsn.openrxlight.api.OpenRxLightV2;
import cn.ggsn.openrxlight.domain.AccountType;
import cn.ggsn.openrxlight.errorx.BizException;
import cn.ggsn.openrxlight.model.chat.Callback;
import cn.ggsn.openrxlight.model.order.ChargeBasicType;
import cn.ggsn.openrxlight.model.order.PayType;
import cn.ggsn.openrxlight.request.Required;
import cn.ggsn.openrxlight.request.Validate;
import cn.ggsn.openrxlight.request.orders.CreateOrderRequest;
import cn.ggsn.openrxlight.request.orders.GetQueueInfoRequest;
import cn.ggsn.openrxlight.response.chat.ChatResponse;
import cn.ggsn.openrxlight.response.orders.QueueStatusCode;
import cn.ggsn.openrxlight.utils.JsonUtils;
import cn.ggsn.rxlight.account.domain.ConsumerAccountExtInfo;
import cn.ggsn.rxlight.account.domain.ConsumerAccountExtInfo.CarInfo;
import cn.ggsn.rxlight.ai.agent.Chatbox;
import cn.ggsn.rxlight.ai.callback.CallbackHandler;
import cn.ggsn.rxlight.ai.domain.RxLightChatMessage;
import lombok.Getter;

public class CreateOrderHandler extends CallbackHandler {

    @Getter
    private static class CreateOrderReq implements Validate {
        @Required
        private Long stationId;
        @Required
        private Long spaceId;
        @Required
        private Long rangeId;
        @Required
        private String spaceNo;
        @Required
        private String plateNo;
        @Required
        private PayType payType;
        @Required
        private Integer chargeBasic;
        @Required
        private ChargeBasicType chargeBasicType;
    }

    public CreateOrderHandler(OpenRxLightV2 openRxLightV2, Chatbox chatbox) {
        super(openRxLightV2, chatbox);
    }

    @Override
    public Stream<ChatResponse> handle(RxLightChatMessage chatMessage) throws Exception {
        Callback callback = chatMessage.getCallback();
        CreateOrderReq req = callback.getVariablesAs(CreateOrderReq.class);
        req.validate();
        Account account = Account
                .getByAccountId(chatMessage.getUserId(), AccountType.CONSUMER)
                .orElseThrow(
                        () -> new BizException(
                                AccountError.AccountNotExist,
                                chatMessage.getUserId()));
        var queueInfo = this.openRxLightV2
                .orders()
                .queueInfo(GetQueueInfoRequest
                        .builder()
                        .stationId(req.stationId)
                        .rangeId(req.rangeId)
                        .build());
        if (Objects.nonNull(queueInfo)) {
            QueueStatusCode statusCode = QueueStatusCode.fromCode(queueInfo.getCode());
            switch (statusCode) {
                case UNSUPPORTED:
                case UNAVAILABLE:
                    return this.chatbox.completion(
                            "Sorry, we cannot serve you now.",
                            null);
                case NOT_NEEDED:
                    break;
                case AVAILABLE:
                    return this.chatbox.completion(
                            "There is currently no available device to serve you, you can wait until one becomes available. Do you want to wait?",
                            null).map(msg -> {
                                msg.setCallback(Callback
                                        .builder()
                                        .type("confirm_queue")
                                        .variables(Map.of("queue_info", JsonUtils.toJsonNode(queueInfo)))
                                        .build());
                                return msg;
                            });
                default:
                    break;
            }
        }
        var resp = this.openRxLightV2
                .orders()
                .createOrder(CreateOrderRequest
                        .builder()
                        .phoneNo(((ConsumerAccountExtInfo) account.getAccountInfo()).getPhoneNo())
                        .chargeBasicType(req.chargeBasicType.getValue())
                        .chargeBasicValue(req.chargeBasic)
                        .stationId(req.stationId)
                        .spaceId(req.spaceId)
                        .carModelId(((ConsumerAccountExtInfo) account.getAccountInfo())
                                .getCarInfo(req.plateNo)
                                .map(CarInfo::getModelId)
                                .orElse(null))
                        .build());
        return Stream.of(ChatResponse
                .builder()
                .callback(Callback
                        .builder()
                        .type("create_order")
                        .variables(Map.of("order_no", JsonNodeFactory.instance.textNode(resp.getOrderNo()),
                                "queue_info", JsonUtils.toJsonNode(resp.getQueueInfo())))
                        .build())
                .build());
    }

    @Override
    public boolean supports(String eventType) {
        return "create_order".equals(eventType);
    }

}
