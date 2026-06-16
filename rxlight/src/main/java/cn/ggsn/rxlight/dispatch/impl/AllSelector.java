package cn.ggsn.rxlight.dispatch.impl;

import java.util.List;
import cn.ggsn.openrxlight.account.domain.Account;
import cn.ggsn.openrxlight.lang.Lists2;
import cn.ggsn.openrxlight.web.RoleType;
import cn.ggsn.rxlight.dispatch.ExecutorSelector;
import io.quarkus.arc.properties.IfBuildProperty;
import jakarta.inject.Singleton;

@IfBuildProperty(name = "rxlight.dispatch.executor.selector.strategy", stringValue = "random")
@Singleton
class AllSelector implements ExecutorSelector {

    @Override
    public List<Account> selectAnExecutor() {
        List<Account> accounts = Account.listByRoleType(Lists2.of(RoleType.EXECUTOR));

        return accounts;
    }

}
