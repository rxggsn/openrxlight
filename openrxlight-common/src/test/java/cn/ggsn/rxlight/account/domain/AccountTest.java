package cn.ggsn.rxlight.account.domain;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import cn.ggsn.openrxlight.account.domain.Account;
import cn.ggsn.openrxlight.domain.AccountType;
import cn.ggsn.openrxlight.domain.SourceType;

@QuarkusTest
class AccountTest {

    private static final String DISPLAY_NAME = "Test User";
    private static final SourceType SOURCE_TYPE = SourceType.WEAPP;

    @Test
    @TestTransaction
    void testPersist() {
        Account account = new Account(DISPLAY_NAME, SOURCE_TYPE, AccountType.CONSUMER);
        account.save();

        assertNotNull(account.id);

        Account fetchedAccount = Account.findById(account.id);
        assertNotNull(fetchedAccount);
        assertEquals(account.getAccountId(), fetchedAccount.getAccountId());
        assertEquals(DISPLAY_NAME, fetchedAccount.getDisplayName());
        assertEquals(SOURCE_TYPE.getValue(), fetchedAccount.getSource());
    }
}