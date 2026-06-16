package cn.ggsn.rxlight.ai.agent.impl.lark.card;

import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import javax.imageio.ImageIO;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.lark.oapi.Client;
import com.lark.oapi.event.cardcallback.P2CardActionTriggerHandler;
import com.lark.oapi.event.cardcallback.model.CallBackCard;
import com.lark.oapi.event.cardcallback.model.CallBackToast;
import com.lark.oapi.event.cardcallback.model.P2CardActionTrigger;
import com.lark.oapi.event.cardcallback.model.P2CardActionTriggerResponse;
import com.lark.oapi.service.cardkit.v1.model.PatchCardElementReq;
import com.lark.oapi.service.cardkit.v1.model.PatchCardElementReqBody;
import com.lark.oapi.service.im.v1.enums.CreateImageImageTypeEnum;
import com.lark.oapi.service.im.v1.model.CreateImageReq;
import com.lark.oapi.service.im.v1.model.CreateImageReqBody;

import cn.ggsn.openrxlight.Constants;
import cn.ggsn.openrxlight.account.domain.Account;
import cn.ggsn.openrxlight.account.error.AccountError;
import cn.ggsn.openrxlight.api.OpenRxLightV2;
import cn.ggsn.openrxlight.domain.AccountType;
import cn.ggsn.openrxlight.domain.ExternalAccount;
import cn.ggsn.openrxlight.domain.ExternalAccountType;
import cn.ggsn.openrxlight.errorx.BizException;
import cn.ggsn.openrxlight.lang.Lists2;
import cn.ggsn.openrxlight.lang.Maps2;
import cn.ggsn.openrxlight.model.billing.PaymentChannel;
import cn.ggsn.openrxlight.model.chat.Callback;
import cn.ggsn.openrxlight.model.chat.RoleType;
import cn.ggsn.openrxlight.model.chat.callback.event.ConfirmOrder;
import cn.ggsn.openrxlight.model.order.PayType;
import cn.ggsn.openrxlight.request.billing.PayForAddedOnCreditRequest;
import cn.ggsn.openrxlight.request.billing.UpgradeCreditPlanRequest;
import cn.ggsn.openrxlight.request.chat.UserMessageType;
import cn.ggsn.openrxlight.response.billing.UpgradeCreditPlanResponse;
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
        private static final String CONFIRM_BUTTON = "confirm";
        private static final String CANCEL_BUTTON = "cancel";
        private static final Gson gson = new Gson();

        private final Queue<RxLightChatMessage> recvQueue;
        private final Client client;
        private final LarkContext context;
        private final OpenRxLightV2 openRxLightV2;
        private final CardTemplateCache cardTemplates;
        private final ApiEndpoint orderApi;
        private final Path fs;

        public CardActionHandler(Queue<RxLightChatMessage> recvQueue, Client client,
                        LarkContext context, OpenRxLightV2 openRxLightV2, CardTemplateCache cardTemplates,
                        Path fs, ApiEndpoint orderApi) {
                this.recvQueue = recvQueue;
                this.client = client;
                this.context = context;
                this.openRxLightV2 = openRxLightV2;
                this.cardTemplates = cardTemplates;
                this.fs = fs;
                this.orderApi = orderApi;
        }

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
                UpgradeCreditPlanResponse payForBillResp = null;
                switch (eventType) {
                        case CallbackEventType.PAY_FOR_ADDED_ON:
                                try {
                                        int creditCount = Integer
                                                        .parseInt(Objects.toString(event.getEvent().getAction()
                                                                        .getFormValue().get("credit_count")));
                                        payForBillResp = this.openRxLightV2.billing()
                                                        .payForAddedOnCredit(PayForAddedOnCreditRequest
                                                                        .builder()
                                                                        .creditCount(creditCount)
                                                                        .currencyType("CNY")
                                                                        .paymentChannel(PaymentChannel.fromString(
                                                                                        Objects.toString(event
                                                                                                        .getEvent()
                                                                                                        .getAction()
                                                                                                        .getFormValue()
                                                                                                        .get("payment_channel"))))
                                                                        .build());
                                } catch (NumberFormatException e) {
                                        var toast = new CallBackToast();
                                        toast.setContent("Credit Count Must Be A Valid Integer");
                                        triggerResp.setToast(toast);
                                        return triggerResp;
                                }
                                break;
                        case CallbackEventType.UPGRADE_CREDIT_PLAN:
                                payForBillResp = this.openRxLightV2.billing().upgradeCreditPlan(UpgradeCreditPlanRequest
                                                .builder()
                                                .currencyType("CNY")
                                                .packageId(Integer
                                                                .parseInt(Objects.toString(event.getEvent().getAction()
                                                                                .getFormValue().get("package"))))
                                                .paymentChannel(PaymentChannel.fromString(
                                                                Objects.toString(event
                                                                                .getEvent()
                                                                                .getAction()
                                                                                .getFormValue()
                                                                                .get("payment_channel"))))
                                                .build());
                                break;
                        case CallbackEventType.BUY_ADDED_ON:
                                var cardTemplate = this.cardTemplates.getTemplate(CallbackEventType.BUY_ADDED_ON);
                                if (Objects.isNull(cardTemplate)) {
                                        var toast = new CallBackToast();
                                        toast.setContent("Internal Error: No card template found for event type "
                                                        + CallbackEventType.BUY_ADDED_ON);
                                        triggerResp.setToast(toast);
                                        return triggerResp;
                                }

                                var card = new CallBackCard();
                                card.setType("raw");

                                JsonElement content = gson.toJsonTree(cardTemplate.getContent().toString());
                                card.setData(content);
                                triggerResp.setCard(card);
                                return triggerResp;
                        case ConfirmOrder.CALLBACK_TYPE:
                                return createOrderAndSendPaymentQrCode(event, triggerResp, account);
                        default:
                                break;
                }

                String callbackId = null;
                if (!Maps2.isEmpty(event.getEvent().getAction().getValue())) {
                        callbackId = (String) event.getEvent().getAction().getValue().remove("callback_id");
                }
                Callback callback = Callback.builder()
                                .type(eventType)
                                .callbackId(callbackId)
                                .variables(Maps2.mapValue(Maps2.merge(
                                                event.getEvent().getAction().getValue(),
                                                event.getEvent().getAction().getFormValue()),
                                                value -> JsonUtils.toJsonNode(value)))
                                .build();

                if (payForBillResp != null) {
                        this.disableButton(event, eventType);
                        callback.getVariables().put("pay_url",
                                        JsonNodeFactory.instance.textNode(payForBillResp.getPayUrl()));
                        callback.getVariables().put("order_no",
                                        JsonNodeFactory.instance.textNode(payForBillResp.getOrderNo()));
                }
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
                                .callback(callback)
                                .build());
                this.context.hasRemaingMessages = true;
                this.disableButton(event, CONFIRM_BUTTON);
                this.disableButton(event, CANCEL_BUTTON);
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

        private void disableButton(P2CardActionTrigger event, String button) {
                var cardId = this.context.getCardIdByLarkMsgId(event.getEvent().getContext().getOpenMessageId());
                try {
                        this.client.cardkit().v1().cardElement()
                                        .patch(PatchCardElementReq.newBuilder()
                                                        .cardId(cardId)
                                                        .elementId(button)
                                                        .patchCardElementReqBody(
                                                                        PatchCardElementReqBody
                                                                                        .newBuilder()
                                                                                        .partialElement("{\"disabled\":true}")
                                                                                        .sequence(1)
                                                                                        .build())
                                                        .build());
                } catch (Exception e) {
                        log.error("Failed to disable cancellation button for card: {}. error message: {}",
                                        cardId, e.getMessage());
                }
        }

}
