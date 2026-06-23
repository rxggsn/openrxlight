package cn.ggsn.rxlight.ai.agent.impl.lark.card;

import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import javax.imageio.ImageIO;

import org.apache.commons.lang.StringUtils;

import com.beust.jcommander.internal.Lists;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.lark.oapi.Client;
import com.lark.oapi.event.cardcallback.P2CardActionTriggerHandler;
import com.lark.oapi.event.cardcallback.model.CallBackCard;
import com.lark.oapi.event.cardcallback.model.CallBackToast;
import com.lark.oapi.event.cardcallback.model.P2CardActionTrigger;
import com.lark.oapi.event.cardcallback.model.P2CardActionTriggerResponse;
import com.lark.oapi.service.im.v1.enums.CreateImageImageTypeEnum;
import com.lark.oapi.service.im.v1.model.CreateImageReq;
import com.lark.oapi.service.im.v1.model.CreateImageReqBody;

import cn.ggsn.openrxlight.Constants;
import cn.ggsn.openrxlight.account.domain.Account;
import cn.ggsn.openrxlight.account.error.AccountError;
import cn.ggsn.openrxlight.domain.AccountType;
import cn.ggsn.openrxlight.domain.ExternalAccount;
import cn.ggsn.openrxlight.domain.ExternalAccountType;
import cn.ggsn.openrxlight.errorx.BizException;
import cn.ggsn.openrxlight.lang.Lists2;
import cn.ggsn.openrxlight.lang.Maps2;
import cn.ggsn.openrxlight.model.chat.Callback;
import cn.ggsn.openrxlight.model.chat.RoleType;
import cn.ggsn.openrxlight.model.chat.callback.event.ConfirmOrder;
import cn.ggsn.openrxlight.model.order.PayType;
import cn.ggsn.openrxlight.request.chat.UserMessageType;
import cn.ggsn.openrxlight.utils.JsonUtils;
import cn.ggsn.openrxlight.utils.QRCode;
import cn.ggsn.openrxlight.web.AuthorizationToken;
import cn.ggsn.rxlight.ai.agent.impl.lark.vo.ConfirmOrderPayment;
import cn.ggsn.rxlight.ai.agent.impl.lark.vo.LarkContext;
import cn.ggsn.rxlight.ai.domain.AppType;
import cn.ggsn.rxlight.ai.domain.RxLightChatMessage;
import cn.ggsn.rxlight.ai.event.CallbackEventType;
import cn.ggsn.rxlight.orders.ApiEndpoint;
import cn.ggsn.rxlight.orders.request.CreateOrderRequest;
import cn.ggsn.rxlight.orders.request.PayForOrderRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CardActionHandler extends P2CardActionTriggerHandler {

        private static final String EVENT_TYPE_NAME = "event_type";
        // private static final String CONFIRM_BUTTON = "confirm";
        // private static final String CANCEL_BUTTON = "cancel";
        private static final Gson gson = new Gson();

        private final Queue<RxLightChatMessage> recvQueue;
        private final Client client;
        private final LarkContext context;
        private final CardTemplateCache cardTemplates;
        private final ApiEndpoint orderApi;
        private final Path fs;

        public CardActionHandler(Queue<RxLightChatMessage> recvQueue, Client client,
                        LarkContext context, CardTemplateCache cardTemplates, Path fs,
                        ApiEndpoint orderApi) {
                this.recvQueue = recvQueue;
                this.client = client;
                this.context = context;
                this.cardTemplates = cardTemplates;
                this.fs = fs;
                this.orderApi = orderApi;
        }

        @SuppressWarnings("null")
        @Override
        public P2CardActionTriggerResponse handle(P2CardActionTrigger event) throws Exception {
                String eventType = (String) event.getEvent().getAction().getValue().get(EVENT_TYPE_NAME);
                var account = Account.getAccountByExternalAccount(
                                event.getEvent().getOperator().getOpenId(),
                                ExternalAccountType.FEISHU,
                                AppType.FEISHU.equals(context.getAppType()) ? AccountType.OPERATOR
                                                : AccountType.CONSUMER)
                                .orElseThrow(() -> new BizException(AccountError.ExternalAccountNotFound));
                var triggerResp = new P2CardActionTriggerResponse();
                // UpgradeCreditPlanResponse payForBillResp = null;
                switch (eventType) {
                        case CallbackEventType.HUMAN_IN_LOOP:
                                List<String> subEventTypes = Lists.newArrayList(StringUtils.split(
                                                (String) event.getEvent().getAction().getValue().get("sub_event_types"),
                                                ","));
                                if (subEventTypes.contains(ConfirmOrder.CALLBACK_TYPE)) {
                                        return createOrderAndSendPaymentQrCode(event, triggerResp, account);
                                }
                        default:
                                break;
                }

                // String callbackId = null;
                // if (!Maps2.isEmpty(event.getEvent().getAction().getValue())) {
                // callbackId = (String)
                // event.getEvent().getAction().getValue().remove("callback_id");
                // }
                Map<String, JsonNode> variables = Maps2.mapValue(Maps2.merge(
                                event.getEvent().getAction().getValue(),
                                event.getEvent().getAction().getFormValue()),
                                value -> JsonUtils.toJsonNode(value));
                Map<String, Callback> callbacks = Maps2.empty();
                variables.forEach((key, value) -> {
                        var newCallback = CallbackVariableTransformer.extractCallback(key, value);
                        if (!Objects.isNull(newCallback)) {
                                String callbackKey = StringUtils
                                                .join(new String[] { newCallback.getType(),
                                                                newCallback.getCallbackId() }, ":");

                                callbacks.computeIfPresent(callbackKey, (_k, curr) -> {
                                        curr.merge(newCallback);
                                        return curr;
                                });

                                callbacks.computeIfAbsent(callbackKey, _k -> newCallback);
                        }
                });

                // Callback callback = Callback.builder()
                // .type(eventType)
                // .callbackId(callbackId)
                // .variables(variables)
                // .build();

                String content = StringUtils.isNotBlank(event.getEvent().getAction().getInputValue())
                                ? event.getEvent().getAction().getInputValue()
                                : null;
                this.recvQueue.offer(RxLightChatMessage.builder()
                                .content(content)
                                .userId(account.getAccountId())
                                .appId(this.context.getAgentId())
                                .messageType(UserMessageType.CALLBACK.getName())
                                .role(RoleType.USER.getName())
                                .createdAt(LocalDateTime.now())
                                .appMessageId(event.getEvent().getContext().getOpenMessageId())
                                .openrxlightAccountId(Lists2.first(Lists2.map(
                                                account.getExternalAccounts(Lists2.of(ExternalAccountType.DEVELOPER)),
                                                ExternalAccount::getExternalAccountId)))
                                .extra(RxLightChatMessage.ExtraInfo.builder()
                                                .location(this.context.getLocation(account.getAccountId()).orElse(null))
                                                .i18n(account.getLanguage()).build())
                                .callback(new RxLightChatMessage.CallbackSet(Sets.newHashSet(callbacks.values())))
                                .build());
                this.context.hasRemaingMessages = true;
                return triggerResp;
        }

        private P2CardActionTriggerResponse createOrderAndSendPaymentQrCode(P2CardActionTrigger event,
                        P2CardActionTriggerResponse triggerResp, Account account) throws Exception {
                var imageCardTemplate = this.cardTemplates.getTemplate("show_image");
                if (Objects.isNull(imageCardTemplate)) {
                        var toast = new CallBackToast();
                        toast.setContent("Internal Error: No card template found for event type show_image");
                        triggerResp.setToast(toast);
                        return triggerResp;
                }
                ConfirmOrderPayment orderReq = JsonUtils.fromMap(
                                Maps2.merge(event.getEvent().getAction().getValue(),
                                                event.getEvent().getAction().getFormValue()),
                                ConfirmOrderPayment.class);
                orderReq.validate();
                AuthorizationToken securityContext = AuthorizationToken.builder().accountId(account.getAccountId())
                                .accountType(account.checkAccountType()).build();
                var resp = this.orderApi.createOrder(
                                CreateOrderRequest.builder().chargeBasicType(orderReq.getChargeBasicType())
                                                .chargeBasicValue(orderReq.getChargeBasicValue())
                                                .payType(PayType.PRE_FEE)
                                                .paymentChannel(orderReq.getPaymentChannel())
                                                .plateNo(orderReq.getPlateNo())
                                                .prepay(orderReq.getPrepayCcy().multiply(BigDecimal.valueOf(100L))
                                                                .intValue())
                                                .spaceId(Long.parseLong(orderReq.getSpace()))
                                                .stationId(Long.parseLong(orderReq.getStation()))
                                                .orderType(orderReq.getOrderType()).build(),
                                securityContext);
                var payResp = this.orderApi.payForOrder(PayForOrderRequest.builder().orderNo(resp.getOrderNo()).build(),
                                securityContext);

                BufferedImage qrCode = QRCode.createQRCode(payResp.getPayByQrCode().getQrCode(), 200, 200);
                Path imageDir = this.fs.resolve("app").resolve(this.context.getAppId()).resolve("images");
                Files.createDirectories(imageDir);
                Path imagePath = imageDir
                                .resolve(LocalDateTime.now().format(Constants.QUERY_DATE_TIME_FORMATTER) + ".png");
                try (OutputStream out = Files.newOutputStream(imagePath)) {
                        ImageIO.write(qrCode, "png", out);
                }

                var larkResp = this.client.im().v1().image().create(
                                CreateImageReq.newBuilder()
                                                .createImageReqBody(CreateImageReqBody.newBuilder()
                                                                .image(imagePath.toFile())
                                                                .imageType(CreateImageImageTypeEnum.MESSAGE)
                                                                .build())
                                                .build());
                if (!larkResp.success()) {
                        log.error("Failed to upload image to Lark , error message: {}, details: {}",
                                        larkResp.getMsg(), JsonUtils.toJson(larkResp.getError().getDetails()));
                        throw new RuntimeException(
                                        String.format("Failed to upload image to Lark , error message: %s, details: %s",
                                                        larkResp.getMsg(),
                                                        JsonUtils.toJson(larkResp.getError().getDetails())));
                }

                imageCardTemplate.replaceVariables(
                                Map.of("image_key", JsonNodeFactory.instance.textNode(larkResp.getData().getImageKey()),
                                                "description",
                                                JsonNodeFactory.instance
                                                                .textNode(payResp.getPayByQrCode().getDescription())),
                                null);

                var imageCard = new CallBackCard();
                imageCard.setType("raw");

                imageCard.setData(gson.toJsonTree(imageCardTemplate.getContent().toString()));
                triggerResp.setCard(imageCard);
                return triggerResp;
        }

}
