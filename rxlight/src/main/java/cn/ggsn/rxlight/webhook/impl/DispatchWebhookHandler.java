package cn.ggsn.rxlight.webhook.impl;

import java.util.List;
import java.util.SortedMap;
import com.google.common.collect.Maps;

import cn.ggsn.openrxlight.account.domain.Account;
import cn.ggsn.openrxlight.config.Config;
import cn.ggsn.openrxlight.config.OpenRxLightApiConfig;
import cn.ggsn.openrxlight.domain.AccountType;
import cn.ggsn.openrxlight.event.WebhookEvent;
import cn.ggsn.openrxlight.event.order.dispatch.DispatchResultEvent;
import cn.ggsn.openrxlight.lang.Lists2;
import cn.ggsn.openrxlight.model.order.OpsOrder;
import cn.ggsn.openrxlight.notification.NotificationReq;
import cn.ggsn.openrxlight.notification.NotificationService;
import cn.ggsn.openrxlight.response.chat.ChatResponse.Attachment;
import cn.ggsn.openrxlight.utils.JsonUtils;
import cn.ggsn.rxlight.webhook.WebhookEventHandler;
import jakarta.inject.Singleton;

@Singleton
class DispatchWebhookHandler implements WebhookEventHandler {
    private final Config config;
    private final NotificationService notificationService;

    public DispatchWebhookHandler(NotificationService notificationService, OpenRxLightApiConfig config) {
        this.notificationService = notificationService;
        this.config = Config.builder()
                .clientId(config.clientId())
                .clientSecret(config.clientSecret())
                .signaturePriKey(config.signaturePrivKey())
                .signaturePubKey(config.signaturePubKey())
                .build();
    }

    @Override
    public void handleEvent(WebhookEvent event) {
        try {
            switch (event.getEventType()) {
                case WebhookEvent.Type.DISPATCH_RESULT:
                    DispatchResultEvent dispatchResultEvent = event.getBody(DispatchResultEvent.class, this.config);
                    SortedMap<String, String> extInfo = Maps.newTreeMap();
                    NotificationReq req = NotificationReq.builder()
                            .content(dispatchResultEvent.getResponse().getContent())
                            .attachments(Lists2.map(dispatchResultEvent.getResponse().getAttachments(),
                                    Attachment::getFileId))
                            .build();
                    switch (dispatchResultEvent.getType()) {
                        case DispatchResultEvent.CREATE_ORDER_TYPE:
                            OpsOrder order = JsonUtils.fromJson(dispatchResultEvent.getBody(), OpsOrder.class);
                            extInfo.put("phone_no", order.getPhoneNo());
                            break;
                        case DispatchResultEvent.FINISH_ORDER_TYPE:
                            OpsOrder finishedOrder = JsonUtils.fromJson(dispatchResultEvent.getBody(), OpsOrder.class);
                            extInfo.put("phone_no", finishedOrder.getPhoneNo());
                            break;
                        default:
                            break;
                    }
                    List<Account> accounts = Account.getAccountByExtInfo(AccountType.CONSUMER, extInfo);

                    Lists2.foreach(accounts, account -> {
                        this.notificationService.sendToAccount(req, account);
                    });
                    break;

                default:
                    break;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean support(Integer eventType) {
        return WebhookEvent.Type.DISPATCH_RESULT == eventType;
    }

}
