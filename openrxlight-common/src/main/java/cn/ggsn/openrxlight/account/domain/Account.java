package cn.ggsn.openrxlight.account.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedMap;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.dialect.type.PostgreSQLJsonPGObjectJsonbType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import cn.ggsn.openrxlight.account.error.AccountError;
import cn.ggsn.openrxlight.account.event.EventConstants;
import cn.ggsn.openrxlight.domain.AccountType;
import cn.ggsn.openrxlight.domain.BaseEntity;
import cn.ggsn.openrxlight.domain.ExternalAccount;
import cn.ggsn.openrxlight.domain.ExternalAccountType;
import cn.ggsn.openrxlight.domain.SourceType;
import cn.ggsn.openrxlight.errorx.BizException;
import cn.ggsn.openrxlight.event.CloudEvent;
import cn.ggsn.openrxlight.event.ContentType;
import cn.ggsn.openrxlight.event.EventBusPublisher;
import cn.ggsn.openrxlight.event.EventBusType;
import cn.ggsn.openrxlight.lang.Lists2;
import cn.ggsn.openrxlight.utils.JsonUtils;
import cn.ggsn.openrxlight.web.RoleType;
import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Table(name = "user_accounts")
@NoArgsConstructor
@JsonNaming(com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Account extends BaseEntity {
    @Column(name = "account_id", nullable = false)
    @Setter
    private UUID accountId;
    @Setter
    @Column(name = "display_name", nullable = true)
    private String displayName;
    @Column(name = "account_type", nullable = false)
    private Integer accountType;
    @Column(name = "avatar", nullable = true, columnDefinition = "TEXT")
    private String avatar;
    @JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @JdbcType(PostgreSQLJsonPGObjectJsonbType.class)
    @Column(name = "account_info", nullable = true)
    @Setter
    @JsonIgnore
    private AccountExtInfo accountInfo;
    @JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(name = "external_accounts", nullable = true)
    @JsonIgnore
    private List<Map<String, Object>> externalAccounts;
    @Setter
    @Column(name = "created_time", nullable = false)
    @JsonIgnore
    private LocalDateTime createdTime;
    @Column(name = "updated_time", nullable = false)
    @JsonIgnore
    private LocalDateTime updatedTime;
    @Column(name = "is_active", nullable = false)
    @JsonIgnore
    private Boolean isActive;
    @Column(name = "source", nullable = false)
    @JsonIgnore
    private int source;
    @Column(name = "last_login_time", nullable = true)
    @JsonIgnore
    private LocalDateTime lastLoginTime;
    @Column(name = "role_types", nullable = true)
    @JdbcTypeCode(org.hibernate.type.SqlTypes.ARRAY)
    @JsonIgnore
    @Setter
    private List<Integer> roleTypes;
    @Column(name = "language", nullable = false)
    private String language;

    @Transient
    @JsonIgnore
    @Setter
    private EventBusPublisher eventBusPublisher;

    public Account(String displayName, SourceType sourceType, AccountType accountType) {
        this.accountId = cn.ggsn.openrxlight.lang.UUID.randomUUID();
        this.displayName = displayName;
        this.source = sourceType.getValue();
        this.createdTime = LocalDateTime.now();
        this.updatedTime = LocalDateTime.now();
        this.isActive = true;
        this.accountType = accountType.getValue();
        if (AccountType.CONSUMER.equals(accountType)) {
            this.roleTypes = Lists2.of(RoleType.GUEST.getValue());
        } else if (AccountType.MERCHANT.equals(accountType)) {
            this.roleTypes = Lists2.of(RoleType.ADMIN.getValue());
        } else if (AccountType.OPERATOR.equals(accountType)) {
            this.roleTypes = Lists2.of(RoleType.OPERATOR.getValue());
        } else {
            this.roleTypes = Lists2.of(RoleType.ANONYMOUS.getValue());
        }
        this.language = "zh";
    }

    public void refreshLastLoginTime() {
        this.lastLoginTime = LocalDateTime.now();
    }

    public void addExternalAccount(ExternalAccount externalAccount) {
        // if (ExternalAccountType.DEVELOPER.equals(externalAccount.getAccountType())) {
        // throw new BizException(AccountError.NotSupportExternalAccountType,
        // "you don't have applied any developer account");
        // }
        if (this.externalAccounts == null) {
            this.externalAccounts = Lists2.of(JsonUtils.toMap(externalAccount));
        } else {
            this.externalAccounts.add(JsonUtils.toMap(externalAccount));
        }
    }

    public boolean hasExternalAccount(ExternalAccount externalAccount) {
        var externalAccounts = Lists2.map(this.externalAccounts, ea -> JsonUtils.fromMap(ea, ExternalAccount.class));
        return Lists2.anyOf(externalAccounts,
                ea -> ea.getAccountType() == externalAccount.getAccountType()
                        && ea.getExternalAccountId().equals(externalAccount.getExternalAccountId()));
    }

    public boolean hasExternalAccount(ExternalAccountType accountType) {
        var externalAccounts = Lists2.map(this.externalAccounts, ea -> JsonUtils.fromMap(ea, ExternalAccount.class));
        return Lists2.anyOf(externalAccounts,
                ea -> ea.getAccountType() == accountType);
    }

    public Optional<ExternalAccount> getExternalAccount(String id, ExternalAccountType accountType) {
        var externalAccounts = Lists2.map(this.externalAccounts, ea -> JsonUtils.fromMap(ea, ExternalAccount.class));
        return Lists2.filter(externalAccounts, ea -> {
            return ea.getAccountType() == accountType
                    && StringUtils.equals(ea.getExternalAccountId(), id);
        }).stream().findFirst();
    }

    public List<ExternalAccount> getExternalAccounts(List<ExternalAccountType> accountTypes) {
        var externalAccounts = Lists2.map(this.externalAccounts, ea -> JsonUtils.fromMap(ea, ExternalAccount.class));
        return Lists2.filter(externalAccounts, ea -> accountTypes.contains(ea.getAccountType()));
    }

    public static Optional<Account> getByAccountId(UUID accountId, AccountType accountType) {
        return QuarkusTransaction.joiningExisting().call(() -> {
            CriteriaBuilder cb = Account.getEntityManager().getCriteriaBuilder();
            var query = cb.createQuery(Account.class);
            var root = query.from(Account.class);
            return Account.getEntityManager()
                    .createQuery(query.select(root)
                            .where(cb.and(
                                    cb.equal(root.get("accountId"), accountId),
                                    cb.equal(root.get("accountType"), accountType.getValue()))))
                    .getResultStream()
                    .findFirst();
        });
    }

    public static Optional<Account> getAccountByExternalAccount(String externalAccountId,
            ExternalAccountType externalAccountType, AccountType accountType) {
        return QuarkusTransaction.joiningExisting().call(() -> {
            if (StringUtils.isBlank(externalAccountId)) {
                return Optional.empty();
            }
            String sql = String.format(
                    "SELECT * FROM user_accounts WHERE external_accounts @> '[{\"external_account_id\": \"%s\", \"account_type\": \"%s\"}]' AND account_type = ?1",
                    externalAccountId, externalAccountType.name().toLowerCase());
            Account result = (Account) Account.getEntityManager().createNativeQuery(sql, Account.class)
                    .setParameter(1, accountType.getValue())
                    .getSingleResultOrNull();
            return Optional.ofNullable(result);
        });
    }

    @SuppressWarnings("unchecked")
    public static List<Account> getAccountByExtInfo(AccountType accountType, SortedMap<String, String> extBody) {
        return QuarkusTransaction.joiningExisting().call(() -> {
            if (extBody == null || extBody.isEmpty()) {
                return Lists2.empty();
            }
            StringBuilder sqlBuilder = new StringBuilder(
                    "SELECT * FROM user_accounts WHERE account_type = ?1 ");
            int index = 2;
            for (String key : extBody.keySet()) {
                sqlBuilder.append(" AND account_info->>'")
                        .append(key)
                        .append("' = ?")
                        .append(index)
                        .append(" ");
                index++;
            }
            String sql = sqlBuilder.toString();
            var query = Account.getEntityManager()
                    .createNativeQuery(sql, Account.class)
                    .setParameter(1, accountType.getValue());
            index = 2;
            for (String key : extBody.keySet()) {
                query.setParameter(index, extBody.get(key));
                index++;
            }

            List<Account> resultList = query.getResultList();
            return resultList;
        });
    }

    public AccountType checkAccountType() {
        return AccountType.fromValue(this.accountType);
    }

    public void addRoleType(RoleType roleType) {
        if (roleType == null || roleType == RoleType.ANONYMOUS) {
            return;
        }
        if (this.roleTypes == null) {
            this.roleTypes = Lists2.of(roleType.getValue());
        } else if (!this.roleTypes.contains(roleType.getValue())) {
            this.roleTypes.add(roleType.getValue());
        }
    }

    public static Optional<Account> getById(Long id) {
        return QuarkusTransaction.joiningExisting().call(() -> Account.findByIdOptional(id));
    }

    public void setInActive() {
        this.isActive = false;
    }

    public void setActive() {
        this.isActive = true;
    }

    @Override
    @Transactional
    public void save() {
        if (this.id == null || this.id == 0) {
            if (Objects.nonNull(this.eventBusPublisher)) {
                var event = new CloudEvent<>(this.accountId.toString(), null,
                        EventConstants.AccountEvent.ACCOUNT_CREATED,
                        EventConstants.AccountEvent.ACCOUNT_TOPIC, ContentType.APPLICATION_JSON);
                event.setData(this);
                this.eventBusPublisher.publish(event, EventBusType.LOCAL);
            }
            QuarkusTransaction.joiningExisting().run(() -> this.persist());
        } else {
            QuarkusTransaction.joiningExisting().run(() -> {
                getEntityManager().merge(this);
            });
        }
    }

    @Override
    @Transactional
    public void delete() {
        if (this.id != null && this.id != 0) {
            super.delete();
        }
    }

    public static Account getAdminAccount() {
        return QuarkusTransaction.joiningExisting().call(() -> Account.getById(1L)
                .orElseThrow(() -> new BizException(AccountError.AccountNotExist, "Admin account")));
    }

    public static List<Account> listByRoleType(List<RoleType> roleTypes) {
        return QuarkusTransaction.joiningExisting().call(() -> {
            CriteriaBuilder cb = Account.getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(Account.class);
            var root = cq.from(Account.class);
            return Account.getEntityManager()
                    .createQuery(cq.select(root)
                            .where(root.get("roleTypes").in(Lists2.map(roleTypes, RoleType::getValue))))
                    .getResultList();
        });
    }
}
