package cn.ggsn.openrxlight.web;

import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;

import cn.ggsn.openrxlight.domain.AccountType;
import cn.ggsn.openrxlight.lang.Maps2;
import io.smallrye.mutiny.Uni;

public class InMemoryTokenStore implements BlockingTokenStore, UniTokenStore {
    private final Map<String, String> inner;

    public InMemoryTokenStore() {
        this.inner = Maps2.empty();
    }

    @Override
    public Optional<AuthorizationToken> getTokenByAccountId(
            String accountName, AccountType accountType) {
        if (StringUtils.isBlank(accountName)) {
            return Optional.empty();
        }
        return Optional.ofNullable(this.inner.get(this.getTokenKey(accountName, accountType)))
                .map(AuthorizationToken::fromJwtToken);
    }

    @Override
    public String setToken(AuthorizationToken token) {
        String jwtToken = token.getJwtToken();
        this.inner.put(this.getTokenKey(token.getAccountId().toString(), token.getAccountType()), jwtToken);
        return jwtToken;
    }

    @Override
    public void deleteTokenByAccountId(String accountName, AccountType accountType) {
        this.inner.remove(this.getTokenKey(accountName, accountType));
    }

    @Override
    public Uni<AuthorizationToken> uniGetTokenByAccountId(String accountId, AccountType accountType) {
        return Uni.createFrom().item(() -> this.getTokenByAccountId(accountId, accountType))
                .map(optional -> optional.orElse(null));
    }

    @Override
    public Uni<String> uniSetToken(AuthorizationToken token) {
        return Uni.createFrom().item(() -> this.setToken(token));
    }

    @Override
    public Uni<Void> uniDeleteTokenByAccountId(String accountId, AccountType accountType) {
        return Uni.createFrom().item(() -> {
            this.deleteTokenByAccountId(accountId, accountType);
            return null;
        });

    }
}
