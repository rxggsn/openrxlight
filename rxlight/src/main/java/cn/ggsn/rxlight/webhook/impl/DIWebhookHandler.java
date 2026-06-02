package cn.ggsn.rxlight.webhook.impl;

import java.util.UUID;

import cn.ggsn.openrxlight.account.domain.Account;
import cn.ggsn.openrxlight.account.error.AccountError;
import cn.ggsn.openrxlight.api.OpenRxLightV2;
import cn.ggsn.openrxlight.domain.AccountType;
import cn.ggsn.openrxlight.errorx.BizException;
import cn.ggsn.openrxlight.event.WebhookEvent;
import cn.ggsn.openrxlight.event.chat.NotifyEvent;
import cn.ggsn.openrxlight.lang.Lists2;
import cn.ggsn.openrxlight.notification.NotificationReq;
import cn.ggsn.openrxlight.notification.NotificationService;
import cn.ggsn.openrxlight.notification.domain.NtySceneType;
import cn.ggsn.openrxlight.request.chat.RecvRequest;
import cn.ggsn.openrxlight.response.chat.ChatResponse;
import cn.ggsn.openrxlight.response.chat.ChatResponse.Attachment;
import cn.ggsn.openrxlight.utils.JsonUtils;
import cn.ggsn.rxlight.webhook.WebhookEventHandler;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
class DIWebhookHandler implements WebhookEventHandler {
    private final OpenRxLightV2 openRxLightV2;
    private final NotificationService notificationService;

    @Override
    public void handleEvent(WebhookEvent event) {
        try {
            NotifyEvent eventBody = event.getBody(NotifyEvent.class, this.openRxLightV2.getConfig());
            var account = Account.getByAccountId(UUID.fromString(eventBody.getUserId()),
                    AccountType.fromValue(eventBody.getAccountType()))
                    .orElseThrow(() -> new BizException(AccountError.AccountNotExist, eventBody.getUserId()));

            RecvRequest request = RecvRequest.builder()
                    .messageId(eventBody.getId())
                    .streamId(eventBody.getStreamId())
                    .build();
            ChatResponse response = this.openRxLightV2
                    .dhforceIntelligence()
                    .recv(request)
                    .reduce(new ChatResponse(),
                            (acc, resp) -> {
                                acc.merge(resp);
                                return acc;
                            });
            this.notificationService.sendToAccount(
                    NotificationReq
                            .builder()
                            .content(JsonUtils.toJsonNode(response))
                            .attachments(Lists2.map(response.getAttachments(), Attachment::getFileId))
                            .sceneType(NtySceneType.DI_CALLBACK)
                            .build(),
                    account);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean support(Integer eventType) {
        return WebhookEvent.Type.DI_NOTIFY == eventType;
    }

}
