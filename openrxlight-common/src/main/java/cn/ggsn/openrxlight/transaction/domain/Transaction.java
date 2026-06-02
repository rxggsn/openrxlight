package cn.ggsn.openrxlight.transaction.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.uuid.Generators;
import cn.ggsn.openrxlight.domain.BaseEntity;
import cn.ggsn.openrxlight.event.EventBusPublisher;
import cn.ggsn.openrxlight.lang.Lists2;
import cn.ggsn.openrxlight.lang.Maps2;
import cn.ggsn.openrxlight.model.Currency;
import cn.ggsn.openrxlight.response.PageResult;
import cn.ggsn.openrxlight.utils.UUIdConverter;
import io.quarkus.narayana.jta.QuarkusTransaction;
import cn.ggsn.openrxlight.transaction.service.payment.PaymentWrapper;
import cn.ggsn.openrxlight.transaction.vo.TransactionFilter;
import cn.ggsn.openrxlight.transaction.vo.TransactionInfo;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.dialect.type.PostgreSQLJsonPGObjectJsonbType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@Table(name = "transactions")
@Entity
public class Transaction extends BaseEntity {
    @Column(name = "transaction_id", nullable = false)
    private UUID transactionId;
    @Column(name = "counterparty_id", nullable = false)
    private UUID counterpartyId;
    @Column(name = "ccy", nullable = false)
    @JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @JdbcType(PostgreSQLJsonPGObjectJsonbType.class)
    private Currency ccy;
    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;
    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime;
    @Column(name = "transaction_info", nullable = false)
    @JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @JdbcType(PostgreSQLJsonPGObjectJsonbType.class)
    private TransactionInfo transactionInfo;
    @Setter
    @Column(name = "counterparty_txn_id", nullable = false)
    private String counterpartyTxnId;
    @Column(name = "status", nullable = false)
    private int status;
    @Column(name = "completed_time", nullable = true)
    private LocalDateTime completedTime;
    @Column(name = "transaction_type", nullable = false)
    private int transactionType;
    // @JsonIgnore
    // @TableField(exist = false)
    // private RxDomainKafkaProducer kafkaProducer;
    @JsonIgnore
    @Transient
    private EventBusPublisher eventBusPublisher;
    @Column(name = "fail_reason", nullable = true)
    private String failReason;
    // NOTES: 对于部分渠道，原生的 transactionId 无法满足其接口字段长度要求，此时会新生成一个 channelTransactionId
    // 来适配其接口
    @Setter
    @Column(name = "channel_transaction_id", nullable = true)
    private String channelTransactionId;
    @Setter
    @Column(name = "related_transaction_id", nullable = true)
    private UUID relatedTransactionId;

    public Transaction(
            UUID counterpartyId,
            String counterpartyTxnId,
            Currency ccy,
            TransactionInfo transactionInfo,
            TransactionType transactionType) {
        this.transactionType = transactionType.getValue();
        this.transactionId = Generators.timeBasedEpochGenerator().generate();
        this.counterpartyId = counterpartyId;
        this.counterpartyTxnId = counterpartyTxnId;
        this.ccy = ccy;
        this.status = TransactionStatus.INITIALIZED.getValue();
        this.transactionInfo = transactionInfo;
        this.createdTime = LocalDateTime.now();
        this.updatedTime = LocalDateTime.now();
    }

    public static PageResult<Transaction> findAllByFilter(TransactionFilter filter) {
        return null;
        // var wrapper = new LambdaQueryChainWrapper<>(transactionRepository);
        // if (filter.getSourceChannelType() != null) {
        // ChannelType channelType = filter.getSourceChannelType();
        // wrapper = wrapper.apply("transaction_info -> 'source' ->
        // 'counterpartyAccount' ->> 'channelType' = {0}",
        // channelType.name());
        // }
        // if (filter.getDestinationChannelType() != null) {
        // ChannelType channelType = filter.getDestinationChannelType();
        // wrapper = wrapper.apply(
        // "transaction_info -> 'destination' -> 'counterpartyAccount' ->> 'channelType'
        // = {0}",
        // channelType.name());
        // }
        // if (filter.getCounterpartyTxnId() != null) {
        // wrapper = wrapper.eq(Transaction::getCounterpartyTxnId,
        // filter.getCounterpartyTxnId());
        // }
        // if (filter.getSourceCounterpartyId() != null) {
        // wrapper = wrapper.apply("transaction_info -> 'source' ->> 'counterpartyId' =
        // {0}",
        // filter.getSourceCounterpartyId().toString());
        // }
        // if (filter.getDestinationCounterpartyId() != null) {
        // wrapper = wrapper.apply("transaction_info -> 'destination' ->>
        // 'counterpartyId' = {0}",
        // filter.getDestinationCounterpartyId().toString());
        // }
        // if (filter.getCompletedTimeFrom() != null) {
        // wrapper = wrapper.ge(Transaction::getCompletedTime,
        // filter.getCompletedTimeFrom());
        // }
        // if (filter.getCompletedTimeTo() != null) {
        // wrapper = wrapper.le(Transaction::getCompletedTime,
        // filter.getCompletedTimeTo());
        // }
        // if (!Lists2.isEmpty(filter.getTransactionStatuses())) {
        // wrapper = wrapper.in(Transaction::getStatus,
        // Lists2.map(filter.getTransactionStatuses(), TransactionStatus::getValue));
        // }
        // if (!Lists2.isEmpty(filter.getTransactionTypes())) {
        // wrapper = wrapper.in(Transaction::getTransactionType,
        // Lists2.map(filter.getTransactionTypes(), TransactionType::getValue));
        // }
        // if (filter.getCreatedTimeFrom() != null) {
        // wrapper = wrapper.ge(Transaction::getCreatedTime,
        // filter.getCreatedTimeFrom());
        // }
        // if (filter.getCreatedTimeTo() != null) {
        // wrapper = wrapper.le(Transaction::getCreatedTime, filter.getCreatedTimeTo());
        // }
        // if (!Lists2.isEmpty(filter.getTransactionIds())) {
        // // if transactionIds is not empty, we should ignore the page info set by the
        // // client
        // filter.setPageNo(1);
        // filter.setPageSize(Lists2.size(filter.getTransactionIds()));
        // wrapper = wrapper.in(Transaction::getTransactionId,
        // filter.getTransactionIds());
        // }
        // //
        // // SFunction<Transaction, ?> columnExtractor =
        // // Transaction.getColumnExtractor(filter.getSortColumn());
        // // if (columnExtractor != null) {
        // // if (filter.isAsc()) {
        // // wrapper = wrapper.orderByAsc(columnExtractor);
        // // } else {
        // // wrapper = wrapper.orderByDesc(columnExtractor);
        // // }
        // // }
        // var page = filter.startPage();
        // page.setReasonable(false);
        // List<Transaction> transactions = wrapper.list();
        // page.close();

        // return new PageResult<>(filter.getPageNo(), filter.getPageSize(),
        // page.getTotal(), transactions);
    }

    public TransactionStatus checkTransactionStatus() {
        return TransactionStatus.fromValue(this.status);
    }

    public boolean isComplete() {
        return this.checkTransactionStatus().isComplete();
    }

    public void setCounterpartyTransaction(CounterpartyTransaction transaction) {
        if (Objects.isNull(transaction)) {
            return;
        }

        this.updateCounterpartyTxnIdAndStatusById(
                transaction.getStatus(),
                transaction.getCounterpartyTxnId(),
                transaction.getChannelTransactionId());
        this.transactionInfo.setCounterpartyTransactionInfo(
                transaction.getCounterpartyTransactionInfo());
        QuarkusTransaction.joiningExisting().run(() -> {
            Transaction.update("transactionInfo = ?1 WHERE transactionId = ?2",
                    this.transactionInfo, this.transactionId);
        });
    }

    public TransactionType checkTransactionType() {
        return TransactionType.fromValue(this.transactionType);
    }

    public void updateStatus(TransactionStatus status) {
        TransactionStatus originStatus = this.checkTransactionStatus();
        this.status = status.getValue();
        this.updatedTime = LocalDateTime.now();
        this.updateCompletedTime(status);

        if (Objects.nonNull(this.eventBusPublisher) && !Objects.equals(originStatus, status)) {
            // this.eventBusPublisher.publish(
            // CloudEvent.builder().data(new TransactionStatusChangeEvent(
            // originStatus,
            // this.checkTransactionStatus(),
            // this.transactionId,
            // System.currentTimeMillis(),
            // this.checkTransactionType(),
            // this.transactionInfo.getDestination()))
            // .datacontenttype(ContentType.APPLICATION_JSON.getType())
            // .subject(EventConstants.TRANSACTION_STATUS_CHANGE_TOPIC)
            // .source(EventConstants.TRANSACTION_GROUP_ID)
            // .dataschema(EventConstants.TRANSACTION_GROUP_ID)
            // .id(this.transactionId.toString())
            // .build(),
            // EventBusType.LOCAL);
            // this.kafkaProducer.send(this.transactionId.toString(), new
            // TransactionStatusChangeEvent(
            // originStatus,
            // this.checkTransactionStatus(),
            // this.transactionId,
            // System.currentTimeMillis(),
            // this.checkTransactionType(),
            // this.transactionInfo.getDestination()),
            // TransactionStatusChangeEvent.TOPIC);
        }
    }

    private void updateCompletedTime(TransactionStatus status) {
        if (status.isComplete() || status.isRefundSuccess()) {
            this.completedTime = LocalDateTime.now();
        }
    }

    public void updateCounterpartyTxnIdAndStatusById(TransactionStatus transactionStatus, String counterpartyTxnId,
            String channelTransactionId) {
        TransactionStatus origin = this.checkTransactionStatus();
        this.status = transactionStatus.getValue();
        this.channelTransactionId = channelTransactionId;
        this.counterpartyTxnId = counterpartyTxnId;
        this.updatedTime = LocalDateTime.now();
        this.updateCompletedTime(transactionStatus);
        this.save();

        if (Objects.nonNull(this.eventBusPublisher) && !Objects.equals(origin, transactionStatus)) {
            // this.eventBusPublisher.publish(
            // CloudEvent.builder().data(new TransactionStatusChangeEvent(
            // origin,
            // this.checkTransactionStatus(),
            // this.transactionId,
            // System.currentTimeMillis(),
            // this.checkTransactionType(),
            // this.transactionInfo.getDestination()))
            // .datacontenttype(ContentType.APPLICATION_JSON.getType())
            // .subject(EventConstants.TRANSACTION_STATUS_CHANGE_TOPIC)
            // .source(EventConstants.TRANSACTION_GROUP_ID)
            // .dataschema(EventConstants.TRANSACTION_GROUP_ID)
            // .id(this.transactionId.toString())
            // .build(),
            // EventBusType.LOCAL);
            // this.kafkaProducer.send(this.transactionId.toString(), new
            // TransactionStatusChangeEvent(
            // origin,
            // this.checkTransactionStatus(),
            // this.transactionId,
            // System.currentTimeMillis(),
            // this.checkTransactionType(),
            // this.transactionInfo.getDestination()),
            // TransactionStatusChangeEvent.TOPIC);
        }
    }

    public void setEventBusPublisher(EventBusPublisher producer) {
        this.eventBusPublisher = producer;
    }

    /**
     * 获取该笔交易在对手方中的交易号
     *
     * @return 该笔交易在对手方中的交易号
     */
    public String getRxLightOrderIdInCounterparty() {
        if (StringUtils.isNotBlank(this.channelTransactionId)) {
            return this.channelTransactionId;
        }
        return UUIdConverter.replaceHyphenWithEmptyChar(this.transactionId);
    }

    public boolean isWithdraw() {
        return TransactionType.WALLET_WITHDRAW.equals(this.checkTransactionType());
    }

    public boolean isRefund() {
        return TransactionType.WECHAT_REFUND.equals(this.checkTransactionType());
    }

    public boolean isRefundSuccess() {
        return TransactionStatus.REFUND.equals(this.checkTransactionStatus());
    }

    public boolean isRefundFailure() {
        TransactionStatus transactionStatus = this.checkTransactionStatus();
        return TransactionStatus.REFUND_FAILURE.equals(transactionStatus);
    }

    public boolean isPartiallyRefundSuccess() {
        return TransactionStatus.PARTIALLY_REFUND.equals(this.checkTransactionStatus());
    }

    public boolean isProcessing() {
        TransactionStatus transactionStatus = this.checkTransactionStatus();
        return TransactionStatus.INITIALIZED.equals(transactionStatus) ||
                TransactionStatus.REFUNDING.equals(transactionStatus) ||
                TransactionStatus.PAYING.equals(transactionStatus);
    }

    public static Optional<Transaction> getByTransactionId(UUID transactionId) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        var query = cb.createQuery(Transaction.class);
        var root = query.from(Transaction.class);
        query.select(root).where(cb.equal(root.get("transactionId"), transactionId));
        return getEntityManager().createQuery(query).getResultStream().findFirst();
    }

    public static Map<UUID, TransactionStatus> tryUpdateCounterpartyTxnStatus(List<UUID> transactionIds) {
        Instance<PaymentWrapper> wrapper = CDI.current().select(PaymentWrapper.class);
        return wrapper.stream().findFirst().map(paymentWrapper -> {
            if (Lists2.isEmpty(transactionIds)) {
                return Maps2.<UUID, TransactionStatus>empty();
            }
            List<Transaction> transactions = Transaction.listByTransactionIds(transactionIds);
            Lists2.foreach(transactions, transaction -> {
                if (!transaction.isComplete()) {
                    paymentWrapper.getCounterpartyTransaction(transaction)
                            .ifPresent(counterpartyTransaction -> {
                                transaction.updateCounterpartyTxnIdAndStatusById(
                                        counterpartyTransaction.getStatus(),
                                        counterpartyTransaction.getCounterpartyTxnId(),
                                        counterpartyTransaction.getChannelTransactionId());
                            });
                }
            });
            return transactions.stream()
                    .collect(Collectors.toMap(Transaction::getTransactionId,
                            transaction -> transaction.checkTransactionStatus()));
        }).orElseGet(Maps2::empty);
    }

    public static List<Transaction> listByTransactionIds(List<UUID> transactionIds) {
        if (Lists2.isEmpty(transactionIds)) {
            return Lists2.empty();
        }

        return QuarkusTransaction.joiningExisting().call(() -> {
            CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
            var query = cb.createQuery(Transaction.class);
            var root = query.from(Transaction.class);
            query.select(root).where(root.get("transactionId").in(transactionIds));
            return getEntityManager().createQuery(query).getResultList();
        });
    }

}
