package cn.ggsn.openrxlight.web;

import cn.ggsn.openrxlight.domain.AccountType;
import java.util.Optional;

public interface BlockingTokenStore {
    String TOKEN_KEY_PREFIX = "token:%d:%s";

    Optional<AuthorizationToken> getTokenByAccountId(String accountId, AccountType accountType);

    String setToken(AuthorizationToken token);

    void deleteTokenByAccountId(String accountId, AccountType accountType);

    default String getTokenKey(String accountId, AccountType accountType) {
        return String.format(TOKEN_KEY_PREFIX, accountType.getValue(), accountId);
    }
}
