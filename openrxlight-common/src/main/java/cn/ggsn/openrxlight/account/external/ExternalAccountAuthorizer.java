package cn.ggsn.openrxlight.account.external;

import java.util.List;
import java.util.Optional;

import cn.ggsn.openrxlight.account.request.LoginRequest;
import cn.ggsn.openrxlight.domain.ExternalAccount;
import cn.ggsn.openrxlight.domain.ExternalAccountType;
import cn.ggsn.openrxlight.lang.Lists2;
import cn.ggsn.openrxlight.web.CommandInjector;

public interface ExternalAccountAuthorizer {
    ExternalAccount authorize(GetExternalAccountReq req);

    boolean support(ExternalAccountType accountType);

    Optional<ExternalAccount> getExternalAccount(GetExternalAccountReq req);

    default List<CommandInjector<GetExternalAccountReq>> getCommandInjectors() {
        return null;
    }

    default void injectCommand(LoginRequest request) {
        List<CommandInjector<GetExternalAccountReq>> injectors = getCommandInjectors();
        Lists2.foreach(injectors, injector -> {
            injector.inject(request);
        });
    }
}
