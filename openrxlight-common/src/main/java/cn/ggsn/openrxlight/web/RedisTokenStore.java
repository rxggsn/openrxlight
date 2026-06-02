package cn.ggsn.openrxlight.web;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;

import cn.ggsn.openrxlight.domain.AccountType;
import cn.ggsn.openrxlight.lang.Lists2;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.vertx.mutiny.redis.client.RedisAPI;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RedisTokenStore implements BlockingTokenStore, UniTokenStore {

    private static final long RXDOMAIN_TOKEN_EXPIRE_HOUR = 720;
    private final RedisAPI redisAPI;

    @Override
    public Optional<AuthorizationToken> getTokenByAccountId(
            String accountName, AccountType accountType) {
        if (StringUtils.isBlank(accountName)) {
            return Optional.empty();
        }
        String key = this.getTokenKey(accountName, accountType);
        return this.redisAPI.get(key)
                .map(resp -> Optional.ofNullable(resp)
                        .map(r -> r.toString(StandardCharsets.UTF_8))
                        .map(AuthorizationToken::fromJwtToken)
                        .orElse(null))
                .runSubscriptionOn(Infrastructure.getDefaultExecutor())
                .await()
                .asOptional()
                .indefinitely();
    }

    @Override
    public String setToken(AuthorizationToken token) {
        String key = this.getTokenKey(token.getAccountId().toString(), token.getAccountType());
        token.setExpireTime(LocalDateTime.now().plusHours(RXDOMAIN_TOKEN_EXPIRE_HOUR).toEpochSecond(ZoneOffset.UTC));
        String jwtToken = token.getJwtToken();
        this.redisAPI
                .setex(key, Long.toString(RXDOMAIN_TOKEN_EXPIRE_HOUR * 3600), jwtToken)
                .runSubscriptionOn(Infrastructure.getDefaultExecutor())
                .await()
                .indefinitely();
        return jwtToken;
    }

    @Override
    public void deleteTokenByAccountId(String accountName, AccountType accountType) {
        String key = this.getTokenKey(accountName, accountType);
        this.redisAPI.del(Lists2.of(key))
                .runSubscriptionOn(Infrastructure.getDefaultExecutor())
                .await()
                .indefinitely();
    }

    @Override
    public Uni<AuthorizationToken> uniGetTokenByAccountId(String accountId, AccountType accountType) {
        if (StringUtils.isBlank(accountId)) {
            return Uni.createFrom().nullItem();
        }
        String key = this.getUniTokenKey(accountId, accountType);
        return this.redisAPI.get(key)
                .map(resp -> resp.toString(StandardCharsets.UTF_8))
                .map(AuthorizationToken::fromJwtToken);
    }

    @Override
    public Uni<String> uniSetToken(AuthorizationToken token) {
        String key = this.getUniTokenKey(token.getAccountId().toString(), token.getAccountType());
        token.setExpireTime(LocalDateTime.now().plusHours(RXDOMAIN_TOKEN_EXPIRE_HOUR).toEpochSecond(ZoneOffset.UTC));
        String jwtToken = token.getJwtToken();
        return this.redisAPI
                .setex(key, Long.toString(RXDOMAIN_TOKEN_EXPIRE_HOUR * 3600), jwtToken)
                .map(resp -> jwtToken);
    }

    @Override
    public Uni<Void> uniDeleteTokenByAccountId(String accountId, AccountType accountType) {
        String key = this.getUniTokenKey(accountId, accountType);
        return this.redisAPI.del(Lists2.of(key)).map(resp -> null);
    }

}
