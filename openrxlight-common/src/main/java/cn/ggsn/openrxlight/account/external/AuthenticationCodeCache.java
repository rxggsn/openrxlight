package cn.ggsn.openrxlight.account.external;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import cn.ggsn.openrxlight.account.domain.Account;
import cn.ggsn.openrxlight.domain.AccountType;
import cn.ggsn.openrxlight.domain.ExternalAccountType;
import cn.ggsn.openrxlight.notification.NotificationReq;
import cn.ggsn.openrxlight.notification.NotificationService;
import cn.ggsn.openrxlight.notification.domain.NtySceneType;
import cn.ggsn.openrxlight.notification.domain.model.SmsContentModel;
import cn.ggsn.openrxlight.utils.JsonUtils;
import io.vertx.mutiny.redis.client.RedisAPI;
import jakarta.inject.Singleton;

@Singleton
public class AuthenticationCodeCache {
    private static final String AUTH_CODE_KEY_PREFIX = "rxlight:auth_code";
    private final RedisAPI redisAPI;
    private final NotificationService notificationService;

    public AuthenticationCodeCache(RedisAPI redisAPI, NotificationService notificationService) {
        this.redisAPI = redisAPI;
        this.notificationService = notificationService;
    }

    public String getCode(AccountType accountType, ExternalAccountType externalAccountType, String externalAccountId) {
        var result = this.redisAPI.getAndAwait(
                String.format("%s:%s:%s:%s", AUTH_CODE_KEY_PREFIX, accountType.name(),
                        externalAccountType.name(), externalAccountId))
                .toString();
        if (result == null || StringUtils.isBlank(result.toString())) {
            return null;
        }
        return result;
    }

    public void sendAuthenticationCode(Account account, ExternalAccountType externalAccountType,
            String externalAccountId) throws Exception {
        String code = this.createCode();
        ObjectNode params = JsonNodeFactory.instance.objectNode();
        params.put("code", code);

        this.notificationService.sendToAccount(NotificationReq.builder()
                .sceneType(NtySceneType.VERIFY_CODE)
                .content(JsonUtils.toJsonNode(SmsContentModel.builder()
                        .params(params)
                        .build()))
                .build(), account);
        this.saveCode(account, externalAccountType, externalAccountId, code);
    }

    private void saveCode(Account account, ExternalAccountType externalAccountType, String externalAccountId,
            String code) {
        this.redisAPI.setexAndAwait(
                String.format("%s:%s:%s:%s", AUTH_CODE_KEY_PREFIX, account.checkAccountType().name(),
                        externalAccountType.name(), externalAccountId),
                "3600", // 1 hour expiration
                code);
    }

    private String createCode() {
        int code = (int) (Math.random() * 1_000_000);
        return String.format("%06d", code);
    }
}
