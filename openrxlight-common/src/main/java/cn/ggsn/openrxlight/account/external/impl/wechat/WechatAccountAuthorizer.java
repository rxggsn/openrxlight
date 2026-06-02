package cn.ggsn.openrxlight.account.external.impl.wechat;

import cn.ggsn.openrxlight.account.domain.Account;
import cn.ggsn.openrxlight.account.error.AccountError;
import cn.ggsn.openrxlight.account.external.ExternalAccountAuthorizer;
import cn.ggsn.openrxlight.account.external.GetExternalAccountReq;
import cn.ggsn.openrxlight.domain.AccountType;
import cn.ggsn.openrxlight.domain.ExternalAccount;
import cn.ggsn.openrxlight.domain.ExternalAccountType;
import cn.ggsn.openrxlight.domain.SourceType;
import cn.ggsn.openrxlight.domain.ExternalAccount.WechatAccountInfo;
import cn.ggsn.openrxlight.errorx.BizException;
import cn.ggsn.openrxlight.utils.JsonUtils;
import io.quarkus.arc.properties.IfBuildProperty;
import io.vertx.mutiny.redis.client.RedisAPI;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Singleton;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Singleton
@IfBuildProperty(name = "rxlight.wechat.mini-program.enabled", stringValue = "true")
class WechatAccountAuthorizer implements ExternalAccountAuthorizer {
    private static final int DEFAULT_AUTH_TIMEOUT = 5;
    private static final String PREFIX = "wechat:account:authorizer:";
    private final RedisAPI redis;
    private final WechatMiniProgramRouter wechatMiniProgramRouter;

    WechatAccountAuthorizer(RedisAPI redis,
            Instance<WechatMiniProgramRouter> wechatMiniProgramRouter) {
        this.redis = redis;
        this.wechatMiniProgramRouter = wechatMiniProgramRouter.get();
    }

    @Override
    public ExternalAccount authorize(GetExternalAccountReq req) {
        if (this.wechatMiniProgramRouter == null) {
            throw new BizException(AccountError.WechatApiNotInitialized);
        }
        WechatMiniProgramApi wechatMiniProgramApi = this.wechatMiniProgramRouter.findByAppId(req.getChannelId());

        var sessionInfo = wechatMiniProgramApi.getSessionInfo(req.getAuthCode());
        if (sessionInfo != null) {
            ExternalAccount externalAccount = ExternalAccount.builder()
                    .accountType(ExternalAccountType.WECHAT_MINI_PROGRAM)
                    .externalAccountId(sessionInfo.getOpenid())
                    .accountInfo(ExternalAccount.WechatAccountInfo
                            .builder()
                            .unionId(sessionInfo.getUnionid())
                            .sessionKey(sessionInfo.getSessionKey())
                            .build())
                    .build();
            var account = Account
                    .getAccountByExternalAccount(externalAccount.getExternalAccountId(),
                            externalAccount.getAccountType(), req.getAccountType())
                    .orElseGet(() -> {
                        Account newAccount = new Account("", SourceType.WEAPP, AccountType.CONSUMER);
                        newAccount.addExternalAccount(externalAccount);
                        newAccount.save();

                        return newAccount;
                    });

            externalAccount.setBoundAccountId(account.getAccountId());
            this.redis.setex(
                    StringUtils.join(PREFIX, externalAccount.getExternalAccountId()),
                    String.valueOf(DEFAULT_AUTH_TIMEOUT * 60),
                    JsonUtils.toJson(externalAccount))
                    .await()
                    .indefinitely();
            return externalAccount;
        }
        return null;
    }

    @Override
    public boolean support(ExternalAccountType accountType) {
        if (this.wechatMiniProgramRouter == null) {
            return false;
        }
        return ExternalAccountType.WECHAT_MINI_PROGRAM.equals(accountType);
    }

    @Override
    public Optional<ExternalAccount> getExternalAccount(GetExternalAccountReq req) {
        ExternalAccount externalAccount = JsonUtils.fromJson(
                this.redis.get(StringUtils.join(PREFIX, req.getExternalAccountId()))
                        .await()
                        .indefinitely()
                        .toString(StandardCharsets.UTF_8),
                ExternalAccount.class);
        if (externalAccount != null) {
            var accountInfo = (WechatAccountInfo) externalAccount.getAccountInfo();
            var phoneNumber = this.wechatMiniProgramRouter
                    .findByAppId(accountInfo.getAppId())
                    .getPhoneNumber(req.getAuthCode());
            accountInfo.setPhoneNo(phoneNumber.getPhoneNumber());
        }
        return Optional.ofNullable(externalAccount);
    }
}
