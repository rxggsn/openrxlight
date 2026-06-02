package cn.ggsn.openrxlight.transaction.domain;

import cn.ggsn.openrxlight.domain.BaseEntity;
import cn.ggsn.openrxlight.transaction.pay.domain.CounterpartyAccount;
import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;

@Table(name = "pay_counterparty_account")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class PaymentCounterpartyAccount extends BaseEntity {
        @Setter
        @Column(name = "account_id", nullable = false)
        private UUID accountId;
        @Column(name = "name", nullable = false)
        private String name;
        @Column(name = "additional_info", nullable = true)
        @JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
        @JdbcType(org.hibernate.dialect.type.PostgreSQLJsonPGObjectJsonbType.class)
        private CounterpartyAccount additionalInfo;
        @Setter
        @Column(name = "channel_type", nullable = false)
        private Short channelType;
        @Column(name = "transaction_types", nullable = true)
        @JdbcTypeCode(org.hibernate.type.SqlTypes.ARRAY)
        private List<Short> transactionTypes;
        @Column(name = "currency_type", nullable = false)
        private String currencyType;

        public ChannelType checkChannelType() {
                return ChannelType.fromValue(channelType);
        }

        public static List<PaymentCounterpartyAccount> listByChannelType(ChannelType wechatPay) {
                return QuarkusTransaction.joiningExisting().call(() -> {
                        var root = getEntityManager()
                                        .getCriteriaBuilder()
                                        .createQuery(PaymentCounterpartyAccount.class);
                        var query = root.from(PaymentCounterpartyAccount.class);
                        return getEntityManager().createQuery(root.select(query)
                                        .where(query.get("channelType")
                                                        .equalTo(wechatPay.getValue())))
                                        .getResultList();
                });

        }

        public static Optional<PaymentCounterpartyAccount> findByAccountId(UUID accountId,
                        ChannelType channelType, String currencyType) {
                return QuarkusTransaction.joiningExisting().call(() -> {
                        var root = getEntityManager()
                                        .getCriteriaBuilder()
                                        .createQuery(PaymentCounterpartyAccount.class);
                        var query = root.from(PaymentCounterpartyAccount.class);
                        return getEntityManager().createQuery(root.select(query)
                                        .where(query.get("channelType")
                                                        .equalTo(channelType.getValue()),
                                                        query.get("accountId")
                                                                        .equalTo(accountId),
                                                        query.get("currencyType")
                                                                        .equalTo(currencyType)))
                                        .getResultStream()
                                        .findFirst();
                });
        }
}
