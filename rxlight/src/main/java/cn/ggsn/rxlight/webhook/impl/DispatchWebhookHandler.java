package cn.ggsn.rxlight.webhook.impl;

import java.util.List;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import cn.ggsn.openrxlight.account.domain.Account;
import cn.ggsn.openrxlight.api.OpenRxLightV2;
import cn.ggsn.openrxlight.event.WebhookEvent;
import cn.ggsn.openrxlight.event.order.dispatch.DispatchResultEvent;
import cn.ggsn.openrxlight.lang.Lists2;
import cn.ggsn.openrxlight.notification.NotificationReq;
import cn.ggsn.openrxlight.notification.NotificationService;
import cn.ggsn.openrxlight.notification.domain.NtySceneType;
import cn.ggsn.openrxlight.utils.JsonUtils;
import cn.ggsn.openrxlight.web.RoleType;
import cn.ggsn.rxlight.webhook.WebhookEventHandler;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor
class DispatchWebhookHandler implements WebhookEventHandler {
    private final NotificationService notificationService;
    private final OpenRxLightV2 openRxLightV2;

    @Override
    public void handleEvent(WebhookEvent event) {
        try {
            var resultEvent = event.getBody(DispatchResultEvent.class, this.openRxLightV2.getConfig());

            switch (resultEvent.getType()) {
                case DispatchResultEvent.SEMI_AUTO:

                    List<Account> executors = Account.listByRoleType(Lists2.of(RoleType.EXECUTOR));

                    if (Lists2.isEmpty(executors)) {
                        Lists2.foreach(Account.listByRoleType(Lists2.of(RoleType.ADMIN)), account -> {
                            this.notificationService.sendToAccount(NotificationReq.builder()
                                    .sceneType(NtySceneType.ADMIN_NTY)
                                    .content(JsonNodeFactory.instance.textNode(String.format(
                                            "站点[%s]目前无运营人员可为用户服务，需立即补充", resultEvent.getStation().getName())))
                                    .build(), account);
                        });
                    } else {
                        this.notificationService.batchSendToAccounts(NotificationReq.builder()
                                .sceneType(NtySceneType.ORDER_START_CHARGE)
                                .content(JsonUtils.toJsonNode(resultEvent))
                                .build(), executors);
                    }

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
