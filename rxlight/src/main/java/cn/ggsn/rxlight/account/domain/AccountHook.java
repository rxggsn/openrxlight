package cn.ggsn.rxlight.account.domain;

import java.util.List;

import cn.ggsn.openrxlight.account.domain.Account;
import cn.ggsn.openrxlight.account.event.EventConstants;
import cn.ggsn.openrxlight.api.OpenRxLightV2;
import cn.ggsn.openrxlight.domain.ExternalAccount;
import cn.ggsn.openrxlight.domain.ExternalAccountType;
import cn.ggsn.openrxlight.event.CloudEvent;
import cn.ggsn.openrxlight.event.EventBusListener;
import cn.ggsn.openrxlight.lang.Lists2;
import cn.ggsn.openrxlight.request.accounts.CreateAccountRequest;
import cn.ggsn.openrxlight.response.accounts.AccountInfo;
import cn.ggsn.openrxlight.utils.JsonUtils;
import cn.ggsn.rxlight.account.event.BatchCreateAccountEvent;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Startup
@Slf4j
@RequiredArgsConstructor
class AccountHook {
        private final OpenRxLightV2 openRxLightV2;

        @EventBusListener(topics = EventConstants.AccountEvent.ACCOUNT_TOPIC, groupId = "account-hook", dataType = BatchCreateAccountEvent.class)
        void onAccountCreated(CloudEvent<BatchCreateAccountEvent> event) {
                List<Account> accounts = event.getData().getAccounts();

                switch (event.getType()) {
                        case EventConstants.AccountEvent.ACCOUNT_CREATED:
                                Lists2.foreach(accounts, account -> this.syncAccount(account));
                                break;
                        default:
                                break;
                }
        }

        private void syncAccount(Account account) {
                try {
                        AccountInfo accountInfo = this.openRxLightV2.authentication().createAccount(CreateAccountRequest
                                        .builder()
                                        .displayName(account.getDisplayName())
                                        .accountType(account.getAccountType())
                                        .externalAccounts(Lists2.map(Lists2.mapNotNull(
                                                        account.getExternalAccounts(),
                                                        ea -> JsonUtils.fromMap(ea, ExternalAccount.class)),
                                                        ea -> CreateAccountRequest.ExternalAccountInfo.builder()
                                                                        .externalAccountId(ea.getExternalAccountId())
                                                                        .accountType(ea.getAccountType().getValue())
                                                                        .build()))
                                        .roleTypes(account.getRoleTypes())
                                        .build());
                        account.addExternalAccount(ExternalAccount.builder()
                                        .externalAccountId(accountInfo.getId().toString())
                                        .accountType(ExternalAccountType.DEVELOPER)
                                        .build());
                        QuarkusTransaction.requiringNew().run(() -> {
                                account.save();
                                // String updateSql = "UPDATE user_accounts SET external_accounts =
                                // COALESCE(external_accounts, '[]'::jsonb) || ? WHERE account_id = ?";
                                // Account.getEntityManager().createNativeQuery(updateSql)
                                // .setParameter(1,
                                // Lists2.of(JsonUtils.toMap(ExternalAccount.builder()
                                // .externalAccountId(accountInfo.getId()
                                // .toString())
                                // .accountType(ExternalAccountType.DEVELOPER)
                                // .build())))
                                // .setParameter(2, account.getAccountId())
                                // .executeUpdate();
                        });

                } catch (Exception e) {
                        log.error("Failed to create account in OpenRxLight platform, accountId={}, error={}",
                                        account.getAccountId(),
                                        e.getMessage(), e);
                }
        }
}
