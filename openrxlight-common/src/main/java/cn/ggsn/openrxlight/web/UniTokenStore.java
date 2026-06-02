package cn.ggsn.openrxlight.web;

import cn.ggsn.openrxlight.domain.AccountType;
import io.smallrye.mutiny.Uni;

public interface UniTokenStore {
    String TOKEN_KEY_PREFIX = "token:%d:%s";

    Uni<AuthorizationToken> uniGetTokenByAccountId(String accountId, AccountType accountType);

    Uni<String> uniSetToken(AuthorizationToken token);

    Uni<Void> uniDeleteTokenByAccountId(String accountId, AccountType accountType);

    default String getUniTokenKey(String accountId, AccountType accountType) {
        return String.format(TOKEN_KEY_PREFIX, accountType.getValue(), accountId);
    }
}
