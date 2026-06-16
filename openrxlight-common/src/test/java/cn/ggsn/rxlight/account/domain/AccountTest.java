package cn.ggsn.rxlight.account.domain;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import cn.ggsn.openrxlight.account.domain.Account;
import cn.ggsn.openrxlight.account.domain.EmailAccountInfo;
import cn.ggsn.openrxlight.domain.AccountType;
import cn.ggsn.openrxlight.domain.ExternalAccount;
import cn.ggsn.openrxlight.domain.ExternalAccountType;
import cn.ggsn.openrxlight.domain.SourceType;
import cn.ggsn.openrxlight.lang.Lists2;

@QuarkusTest
class AccountTest {

    private static final String DISPLAY_NAME = "Test User";
    private static final SourceType SOURCE_TYPE = SourceType.WEAPP;

    @Test
    @TestTransaction
    void testPersist() {
        Account account = new Account(DISPLAY_NAME, SOURCE_TYPE, AccountType.CONSUMER);
        account.save();
        account.addExternalAccount(ExternalAccount.builder().externalAccountId("id")
                .accountType(ExternalAccountType.EMAIL)
                .accountInfo(EmailAccountInfo.builder().password("password").salt("salt").build())
                .build());

        assertNotNull(account.id);

        Account fetchedAccount = Account.findById(account.id);
        assertNotNull(fetchedAccount);
        assertEquals(account.getAccountId(), fetchedAccount.getAccountId());
        assertEquals(DISPLAY_NAME, fetchedAccount.getDisplayName());
        assertEquals(SOURCE_TYPE.getValue(), fetchedAccount.getSource());
        List<ExternalAccount> externalAccounts = account.getExternalAccounts(Lists2.of(ExternalAccountType.EMAIL));
        assertNotNull(externalAccounts);
        assertTrue(!externalAccounts.isEmpty());
        assertEquals(externalAccounts.get(0).getExternalAccountId(), "id");
        assertTrue(externalAccounts.get(0).getAccountInfo() instanceof EmailAccountInfo);

        Account.deleteById(account.id);
    }
}