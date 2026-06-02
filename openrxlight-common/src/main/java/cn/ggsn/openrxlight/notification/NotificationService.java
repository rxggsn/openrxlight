package cn.ggsn.openrxlight.notification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import cn.ggsn.openrxlight.account.domain.Account;
import cn.ggsn.openrxlight.domain.AccountType;
import cn.ggsn.openrxlight.lang.Lists2;
import cn.ggsn.openrxlight.notification.domain.ChannelConfiguration;
import cn.ggsn.openrxlight.notification.domain.MessageRecord;
import cn.ggsn.openrxlight.notification.domain.NtyTemplate;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class NotificationService {

    private final Instance<NotificationSender> senders;

    public NotificationService(@Any Instance<NotificationSender> senders) {
        this.senders = senders;
    }

    public void sendToAccount(NotificationReq req, Account account) {
        req.validate();

        List<NtyTemplate> templates = NtyTemplate.getBySceneTypeAndAccountType(req.getSceneType(),
                AccountType.fromValue(account.getAccountType()));
        List<ChannelConfiguration> channelConfigurations = ChannelConfiguration
                .findByIds(Lists2.map(templates, NtyTemplate::getConfigId));
        var group = Lists2.group(templates, NtyTemplate::getConfigId);

        List<CompletableFuture<List<MessageRecord>>> futures = Lists2.mapNotNull(channelConfigurations, config -> {
            return this.selectSender(config).map(
                    sender -> {
                        return CompletableFuture
                                .supplyAsync(() -> sender.sendResponse(req, config, group.get(config.id), account));
                    }).orElse(null);
        });
        if (Lists2.isEmpty(futures)) {
            return;
        }
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenAccept((result) -> {
                log.debug("Notification sent to account {} completed.", account.getAccountId());
                return;
            }).get();

            var records = Lists2.flatMapNotNull(futures, future -> {
                try {
                    return future.get();
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Failed to send notification to account {}: {}", account.getAccountId(), e.getMessage());
                    return null;
                }
            });

            Lists2.foreach(records, r -> {
                r.setCreatedTime(LocalDateTime.now());
                r.save();
            });
        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to send notification to account {}: {}", account.getAccountId(), e.getMessage());
        }
    }

    private Optional<NotificationSender> selectSender(ChannelConfiguration config) {
        return this.senders.stream()
                .filter(sender -> sender.supports(config.checkChannelType()))
                .findFirst();
    }
}
