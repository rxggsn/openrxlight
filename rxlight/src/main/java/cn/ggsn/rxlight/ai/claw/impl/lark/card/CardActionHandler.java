package cn.ggsn.rxlight.ai.claw.impl.lark.card;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Queue;

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

import cn.ggsn.openrxlight.account.domain.Account;
import cn.ggsn.openrxlight.account.error.AccountError;
import cn.ggsn.openrxlight.api.OpenRxLightV2;
import cn.ggsn.openrxlight.domain.AccountType;
import cn.ggsn.openrxlight.domain.ExternalAccountType;
import cn.ggsn.openrxlight.errorx.BizException;
import cn.ggsn.openrxlight.event.chat.CallbackEventType;
import cn.ggsn.openrxlight.lang.Maps2;
import cn.ggsn.openrxlight.model.billing.PaymentChannel;
import cn.ggsn.openrxlight.model.chat.Callback;
import cn.ggsn.openrxlight.model.chat.RoleType;
import cn.ggsn.openrxlight.request.billing.PayForAddedOnCreditRequest;
import cn.ggsn.openrxlight.request.billing.UpgradeCreditPlanRequest;
import cn.ggsn.openrxlight.request.chat.UserMessageType;
import cn.ggsn.openrxlight.response.billing.UpgradeCreditPlanResponse;
import cn.ggsn.openrxlight.utils.JsonUtils;
import cn.ggsn.rxlight.ai.claw.impl.lark.vo.LarkContext;
import cn.ggsn.rxlight.ai.domain.RxLightChatMessage;
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

        public CardActionHandler(Queue<RxLightChatMessage> recvQueue, Client client,
                        LarkContext context, OpenRxLightV2 openRxLightV2, CardTemplateCache cardTemplates) {
                this.recvQueue = recvQueue;
                this.client = client;
                this.context = context;
                this.openRxLightV2 = openRxLightV2;
                this.cardTemplates = cardTemplates;
        }

        @Override
        public P2CardActionTriggerResponse handle(P2CardActionTrigger event) throws Exception {
                String eventType = (String) event.getEvent().getAction().getValue().get(EVENT_TYPE_NAME);
                var account = Account.getAccountByExternalAccount(
                                event.getEvent().getOperator().getOpenId(),
                                ExternalAccountType.FEISHU,
                                AccountType.OPERATOR)
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
                        default:
                                break;
                }

                Callback callback = Callback.builder()
                                .type(eventType)
                                .variables(Maps2.mapValue(Maps2.merge(
                                                event.getEvent().getAction().getValue(),
                                                event.getEvent().getAction().getFormValue()),
                                                value -> JsonUtils.toJsonNode(value)))
                                .build();

                if (StringUtils.isNotBlank(event.getEvent().getAction().getInputValue())) {
                        callback.getVariables().put("prompt",
                                        JsonNodeFactory.instance
                                                        .textNode(event.getEvent().getAction().getInputValue()));
                }
                if (payForBillResp != null) {
                        this.disableButton(event, eventType);
                        callback.getVariables().put("pay_url",
                                        JsonNodeFactory.instance.textNode(payForBillResp.getPayUrl()));
                        callback.getVariables().put("order_no",
                                        JsonNodeFactory.instance.textNode(payForBillResp.getOrderNo()));
                }

                this.recvQueue.offer(RxLightChatMessage.builder()
                                .userId(account.getAccountId())
                                .appId(this.context.getAgentId())
                                .messageType(UserMessageType.CALLBACK.getName())
                                .role(RoleType.USER.getName())
                                .createdAt(LocalDateTime.now())
                                .appMessageId(event.getEvent().getContext().getOpenMessageId())
                                .callback(callback)
                                .build());
                this.context.hasRemaingMessages = true;
                this.disableButton(event, CONFIRM_BUTTON);
                this.disableButton(event, CANCEL_BUTTON);
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
