package cn.ggsn.rxlight.ai.agent.impl.lark;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang.StringUtils;

import cn.ggsn.openrxlight.Constants;
import cn.ggsn.openrxlight.account.domain.Account;
import cn.ggsn.openrxlight.domain.ExternalAccountType;
import cn.ggsn.openrxlight.event.EventBusPublisher;
import cn.ggsn.openrxlight.lang.Lists2;
import cn.ggsn.openrxlight.notification.NotificationReq;
import cn.ggsn.openrxlight.notification.NotificationSender;
import cn.ggsn.openrxlight.notification.domain.ChannelConfiguration;
import cn.ggsn.openrxlight.notification.domain.ChannelConfiguration.ChannelType;
import cn.ggsn.openrxlight.response.chat.ChatResponse;
import cn.ggsn.openrxlight.utils.JsonUtils;
import cn.ggsn.openrxlight.notification.domain.MessageRecord;
import cn.ggsn.openrxlight.notification.domain.MessageStatus;
import cn.ggsn.openrxlight.notification.domain.NtySceneType;
import cn.ggsn.openrxlight.notification.domain.NtyTemplate;
import cn.ggsn.openrxlight.notification.domain.channel.FeiShuApp;
import cn.ggsn.rxlight.ai.agent.impl.lark.vo.LarkAccountInfo;
import cn.ggsn.rxlight.ai.domain.AgentApp;
import cn.ggsn.rxlight.ai.domain.AppType;
import cn.ggsn.rxlight.ai.domain.RxLightChatMessage;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
@RequiredArgsConstructor
class LarkBotNotificationSender implements NotificationSender {
    private final EventBusPublisher eventBusPublisher;

    @Override
    public List<MessageRecord> sendResponse(NotificationReq req, ChannelConfiguration config,
            List<NtyTemplate> templates, Account account) {
        var appAccounts = config.getAccounts(FeiShuApp.class);
        var futures = Lists2.flatMap(
                account.getExternalAccounts(Lists2.of(ExternalAccountType.FEISHU)),
                externalAccount -> {
                    LarkAccountInfo accountInfo = (LarkAccountInfo) externalAccount.getAccountInfo();
                    if (!Lists2.anyOf(appAccounts, acc -> StringUtils.equals(acc.getAppId(), accountInfo.getAppId()))) {
                        log.warn("Lark account info is null for account {}, skipping notification",
                                account.getAccountId());
                        return null;
                    }

                    return Lists2.flatMap(templates, template -> Lists2.mapNotNull(
                            Lists2.filter(appAccounts,
                                    app -> app.supports(NtySceneType.fromCode(template.getSceneType()))),
                            app -> {
                                var larkbot = AgentApp.findByAppId(app.getAppId(), AppType.FEISHU)
                                        .flatMap(agentApp -> {
                                            try {
                                                return Optional
                                                        .of(new LarkBot(agentApp.getId(), app.getAppId(),
                                                                app.getAppSecret(),
                                                                null,
                                                                null, this.eventBusPublisher,
                                                                null, null, null, true, null,
                                                                AppType.FEISHU));
                                            } catch (IOException e) {
                                                log.error("Failed to create LarkBot for appId {}: {}",
                                                        app.getAppId(),
                                                        e.getMessage());
                                                return Optional.empty();
                                            }
                                        })
                                        .orElseThrow(() -> new RuntimeException(
                                                "Failed to create LarkBot for appId " + app.getAppId()));
                                return CompletableFuture.supplyAsync(() -> {
                                    try {
                                        switch (template.checkSceneType()) {
                                            case DI_CALLBACK:
                                                ChatResponse resp = JsonUtils.fromJson(req.getContent(),
                                                        ChatResponse.class);
                                                RxLightChatMessage msg = RxLightChatMessage.fromChatResponse(resp);
                                                msg.setUserId(account.getAccountId());
                                                larkbot.send(msg);
                                                return MessageRecord
                                                        .builder()
                                                        .accountId(account.getAccountId())
                                                        .content(JsonUtils.toJson(resp.getContent()))
                                                        .status(MessageStatus.SEND_SUCCESS.getCode())
                                                        .templateId(template.id)
                                                        .build();
                                            case MODIFY_RESERVATION:
                                                break;
                                            case ORDER_END_CHARGE:
                                            case ORDER_START_CHARGE:
                                                var msg1 = RxLightChatMessage.fromChatResponse(
                                                        ChatResponse.builder()
                                                                .object(Constants.COMPLETE_CHUNK)
                                                                .created(System.currentTimeMillis())
                                                                .choices(
                                                                        Lists2.of(ChatResponse.Choice.builder()
                                                                                .delta(ChatResponse.Delta.builder()
                                                                                        .content(req.getContent())
                                                                                        .build())
                                                                                .finishReason("stop")
                                                                                .build()))
                                                                .build());
                                                msg1.setUserId(account.getAccountId());
                                                larkbot.send(msg1);
                                                return MessageRecord
                                                        .builder()
                                                        .accountId(account.getAccountId())
                                                        .content(JsonUtils.toJson(req.getContent()))
                                                        .status(MessageStatus.SEND_SUCCESS.getCode())
                                                        .templateId(template.id)
                                                        .build();
                                            case VERIFY_CODE:
                                                break;
                                            default:
                                                break;

                                        }

                                        return null;
                                    } catch (Exception e) {
                                        log.error("Failed to send Lark notification for account {}: {}",
                                                account.getAccountId(),
                                                e.getMessage());
                                        return null;
                                    }
                                });
                            }));
                });

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        return Lists2.mapNotNull(futures, CompletableFuture::join);
    }

    @Override
    public boolean supports(ChannelType checkChannelType) {
        return ChannelType.FEI_SHU_ROBOT.equals(checkChannelType);
    }

    @Override
    public List<MessageRecord> batchSendResponse(NotificationReq notificationReq, ChannelConfiguration config,
            List<NtyTemplate> list, List<Account> accounts) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'batchSendResponse'");
    }

}
