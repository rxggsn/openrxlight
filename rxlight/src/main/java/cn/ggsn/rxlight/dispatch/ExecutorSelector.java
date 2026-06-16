package cn.ggsn.rxlight.dispatch;

import java.util.List;

import cn.ggsn.openrxlight.account.domain.Account;

public interface ExecutorSelector {

    List<Account> selectAnExecutor();
    
}
