package cn.ggsn.rxlight.ai.domain;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.dialect.type.PostgreSQLJsonPGObjectJsonbType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import cn.ggsn.openrxlight.api.OpenRxLightV2;
import cn.ggsn.openrxlight.audio.AudioRecognizer;
import cn.ggsn.openrxlight.errorx.ErrorResponse;
import cn.ggsn.openrxlight.errorx.billing.BillingErrorCode;
import cn.ggsn.openrxlight.event.EventBusPublisher;
import cn.ggsn.openrxlight.lang.Lists2;
import cn.ggsn.openrxlight.model.chat.RoleType;
import cn.ggsn.openrxlight.request.chat.ChatRequest;
import cn.ggsn.openrxlight.request.chat.UserMessageType;
import cn.ggsn.openrxlight.response.chat.ChatResponse;
import cn.ggsn.openrxlight.translator.Translator;
import cn.ggsn.rxlight.ai.agent.AgentBot;
import cn.ggsn.rxlight.ai.agent.impl.lark.LarkBot;
import cn.ggsn.rxlight.ai.domain.credentials.LarkCredential;
import cn.ggsn.rxlight.ai.domain.credentials.OpenRxLightCredential;
import cn.ggsn.rxlight.orders.ApiEndpoint;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.reactivex.Flowable;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "agent_app")
@Entity
@Slf4j
public class AgentApp extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "app_id", nullable = false)
    private String appId;
    @Column(name = "app_secret", nullable = false)
    private String appSecret;
    @Column(name = "app_type", nullable = false)
    private Integer appType;
    @Column(name = "credential", nullable = true)
    @JdbcType(PostgreSQLJsonPGObjectJsonbType.class)
    @JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    private Credential credential;

    @Transient
    @Setter
    @JsonIgnore
    private AgentBot bot;

    @Transient
    @JsonIgnore
    @Setter
    private OpenRxLightV2 client;

    @Column(name = "openrxlight", nullable = true)
    @JdbcType(PostgreSQLJsonPGObjectJsonbType.class)
    @JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    private Credential openrxlight;

    @Transient
    @JsonIgnore
    @Setter
    private Path fs;

    @Transient
    @JsonIgnore
    @Setter
    private AudioRecognizer audioRecognizer;

    @Transient
    @JsonIgnore
    @Setter
    private Translator translator;
    @Transient
    @JsonIgnore
    @Setter
    private ApiEndpoint orderApi;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
    public interface Credential {
    }

    public static Optional<AgentApp> getById(Integer appId) {
        return find("id", appId).firstResultOptional();
    }

    private void init() {
        if (this.client == null && this.openrxlight != null) {
            var cred = (OpenRxLightCredential) this.openrxlight;
            this.client = cred
                    .getInner()
                    .newClientV2();
        }

        if (this.fs == null) {
            try {
                Path path = Path.of(System.getProperty("user.home"), ".rxlight", "app_files", String.valueOf(this.id));
                path.toFile().mkdirs();
                this.fs = path;
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize file system", e);
            }
        }
    }

    public Flowable<ChatResponse> handleMessage(RxLightChatMessage msg) throws Exception {
        this.init();
        // if (AppType.APP.equals(this.checkAppType())) {
        // if (msg.getExtra() == null ||
        // StringUtils.isBlank(msg.getExtra().getLocation())) {
        // throw new BizException(CommonErrorCode.LocationRequired, "location is
        // required");
        // }
        // }
        return this.client
                .dhforceIntelligence()
                .chat(ChatRequest
                        .builder()
                        .messageId(msg.getAppMessageId())
                        .callbacks(Optional.ofNullable(msg.getCallback()).map(callbacks -> callbacks.getCallbacks())
                                .orElse(null))
                        .contextId(msg.getContextId())
                        .messageType(msg.getMessageType())
                        .query(msg.getContent())
                        .appId(this.appId)
                        .userId(msg.getOpenrxlightAccountId())
                        .location(Optional.ofNullable(msg.getExtra())
                                .map(RxLightChatMessage.ExtraInfo::getLocation)
                                .orElse(null))
                        .i18n(Optional.ofNullable(msg.getExtra())
                                .flatMap(extra -> Optional.ofNullable(extra.getI18n())).orElse(null))
                        .build());

    }

    public static List<AgentApp> findApps(List<AppType> list) {
        CriteriaBuilder cb = AgentApp.getEntityManager().getCriteriaBuilder();
        var query = cb.createQuery(AgentApp.class);
        var root = query.from(AgentApp.class);
        List<Integer> codes = Lists2.map(list, AppType::getCode);
        return AgentApp.getEntityManager()
                .createQuery(query.select(root)
                        .where(root.get("appType").in(codes)))
                .getResultList();
    }

    private void tryReply(RxLightChatMessage question, RxLightChatMessage cm) {
        try {
            if (cm == null) {
                return;
            }
            if (this.bot == null) {
                EventBusPublisher eventBusPublisher = CDI.current().select(EventBusPublisher.class).get();
                this.initBot(eventBusPublisher);
            }

            if (UserMessageType.AUDIO.getName().equals(question.getMessageType())
                    && UserMessageType.TEXT.getName().equals(cm.getMessageType())) {
                cm.setMessageType(UserMessageType.AUDIO.getName());
            }
            this.bot.send(cm);
        } catch (Exception e) {
            log.error("Failed to send message through bot for appId: {}", this.getId(), e);
        }
    }

    private void initBot(EventBusPublisher eventBusPublisher) throws Exception {
        switch (AppType.fromValue(this.appType)) {
            case FEISHU:
            case APP:
                var cred = (LarkCredential) this.credential;
                this.bot = new LarkBot(
                        this.id,
                        this.appId,
                        this.appSecret,
                        cred.getVerificationToken(),
                        cred.getEncryptKey(),
                        eventBusPublisher,
                        this.client,
                        this.audioRecognizer,
                        this.translator,
                        false, this.orderApi, AppType.fromValue(this.appType));
                break;
            default:
                break;
        }
    }

    public Runnable startBot(EventBusPublisher eventBusPublisher, Translator translator,
            AudioRecognizer audioRecognizer,
            ApiEndpoint orderApi) throws Exception {
        this.translator = translator;
        this.audioRecognizer = audioRecognizer;
        this.orderApi = orderApi;
        if (this.client == null) {
            this.init();
        }
        if (this.bot == null) {
            this.initBot(eventBusPublisher);
        }
        return () -> {
            var executor = java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor();
            this.bot.recv().forEachOrdered(msg -> {
                // try {
                // var cm = RxLightChatMessage
                // .fromChatResponse(this.handleMessage(msg).reduce(new ChatResponse(), (acc,
                // curr) -> {
                // acc.merge(curr);
                // return acc;
                // }));
                // cm.setAppId(this.getId());
                // cm.setUserId(msg.getUserId());
                if (UserMessageType.LOCATION.getName().equals(msg.getMessageType())) {
                    return;
                }
                CompletableFuture.supplyAsync(() -> {
                    try {
                        return RxLightChatMessage
                                .fromChatResponse(
                                        this.handleMessage(msg)
                                                .reduce((acc, curr) -> {
                                                    if (acc == null) {
                                                        return curr;
                                                    }
                                                    acc.merge(curr);
                                                    return acc;
                                                })
                                                .timeout(600, TimeUnit.SECONDS)
                                                .blockingGet());
                    } catch (ErrorResponse errorResponse) {
                        if (BillingErrorCode.InsufficientCredit.getValue() == errorResponse.getCode()) {
                            return RxLightChatMessage.builder()
                                    .content(
                                            "Your Credit has ran out. You can Buy Added-On Or Higher Version Credit Package")
                                    .appMessageId(msg.getAppMessageId())
                                    .contextId(msg.getContextId())
                                    .role(RoleType.ASSISTANT.getName())
                                    .createdAt(msg.getCreatedAt())
                                    .messageType(UserMessageType.TEXT.getName())
                                    .appId(msg.getAppId()).build();
                        } else if (BillingErrorCode.CreditPackageExpired.getValue() == errorResponse.getCode()) {
                            return RxLightChatMessage.builder()
                                    .content("Your Credit has been expired")
                                    .appMessageId(msg.getAppMessageId())
                                    .contextId(msg.getContextId())
                                    .role(RoleType.ASSISTANT.getName())
                                    .createdAt(msg.getCreatedAt())
                                    .messageType(UserMessageType.TEXT.getName())
                                    .appId(msg.getAppId()).build();
                        } else {
                            throw errorResponse;
                        }
                    } catch (RuntimeException e) {
                        log.error("app {} process message {} occurs error", this.appId, msg.getAppMessageId(), e);
                        // if (e instanceof BizException) {
                        // if (((BizException) e).getCode() ==
                        // CommonErrorCode.LocationRequired.getValue()) {
                        // return RxLightChatMessage
                        // .builder()
                        // .appId(this.id)
                        // .userId(msg.getUserId())
                        // .content("Location is required")
                        // .messageType(UserMessageType.TEXT.getName())
                        // .createdAt(LocalDateTime.now())
                        // .role(RoleType.ASSISTANT.getName())
                        // .appMessageId(msg.getAppMessageId())
                        // .build();
                        // }
                        // }
                        throw e;
                    } catch (Exception e) {
                        log.error("app {} process message {} occurs error", this.appId, msg.getAppMessageId(), e);
                        throw new RuntimeException(e);
                    }
                }).thenAcceptAsync((cm) -> {
                    if (cm != null) {
                        cm.setAppId(this.getId());
                        cm.setUserId(msg.getUserId());
                        cm.setAppMessageId(msg.getAppMessageId());
                        this.tryReply(msg, cm);
                        cm.detachAttachments();
                        cm.save();
                    }
                }, executor);
                // CompletableFuture.runAsync(() -> this.tryReply(cm), executor)
                // .thenAcceptAsync((a) -> {
                // cm.save();
                // }, executor)
                // .exceptionally((e) -> {
                // if (e instanceof BizException) {
                // throw (BizException) e;
                // } else if (e instanceof RuntimeException) {
                // throw (RuntimeException) e;
                // } else {
                // throw new RuntimeException(e);
                // }
                // });

                // } catch (RuntimeException e) {
                // throw e;
                // } catch (Exception e) {
                // throw new RuntimeException(e);
                // }
            });
            log.error("claw {} stopped", this.appId);
        };

    }

    public static Optional<AgentApp> findByAppId(String appId, AppType appType) {
        return find("appId = ?1 and appType = ?2", appId, appType.getCode()).firstResultOptional();
    }
}
