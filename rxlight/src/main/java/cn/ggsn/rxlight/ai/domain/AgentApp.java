package cn.ggsn.rxlight.ai.domain;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.imageio.ImageIO;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.dialect.type.PostgreSQLJsonPGObjectJsonbType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import cn.ggsn.openrxlight.Constants;
import cn.ggsn.openrxlight.api.OpenRxLightV2;
import cn.ggsn.openrxlight.audio.AudioRecognizer;
import cn.ggsn.openrxlight.errorx.BizException;
import cn.ggsn.openrxlight.errorx.CommonErrorCode;
import cn.ggsn.openrxlight.errorx.ErrorResponse;
import cn.ggsn.openrxlight.errorx.billing.BillingErrorCode;
import cn.ggsn.openrxlight.event.EventBusPublisher;
import cn.ggsn.openrxlight.lang.Lists2;
import cn.ggsn.openrxlight.lang.Maps2;
import cn.ggsn.openrxlight.model.billing.BillingInfo;
import cn.ggsn.openrxlight.model.billing.PaymentChannel;
import cn.ggsn.openrxlight.model.chat.Callback;
import cn.ggsn.openrxlight.model.chat.RoleType;
import cn.ggsn.openrxlight.request.chat.ChatRequest;
import cn.ggsn.openrxlight.request.chat.UserMessageType;
import cn.ggsn.openrxlight.response.chat.ChatResponse;
import cn.ggsn.openrxlight.translator.Translator;
import cn.ggsn.openrxlight.utils.JsonUtils;
import cn.ggsn.openrxlight.utils.QRCode;
import cn.ggsn.rxlight.ai.agent.AgentBot;
import cn.ggsn.rxlight.ai.agent.impl.lark.LarkBot;
import cn.ggsn.rxlight.ai.domain.credentials.LarkCredential;
import cn.ggsn.rxlight.ai.domain.credentials.OpenRxLightCredential;
import cn.ggsn.rxlight.ai.event.CallbackEventType;
import cn.ggsn.rxlight.orders.ApiEndpoint;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.reactivex.Flowable;
import io.vertx.redis.client.RedisAPI;
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
        if (AppType.APP.equals(this.checkAppType())) {
            if (msg.getExtra() == null || StringUtils.isBlank(msg.getExtra().getLocation())) {
                throw new BizException(CommonErrorCode.LocationRequired, "location is required");
            }
        }
        return this.client
                .dhforceIntelligence()
                .chat(ChatRequest
                        .builder()
                        .messageId(msg.getAppMessageId())
                        .callback(msg.getCallback())
                        .contextId(msg.getContextId())
                        .messageType(msg.getMessageType())
                        .query(msg.getContent())
                        .appId(this.appId)
                        .userId(msg.getOpenrxlightAccountId())
                        .location(Optional.ofNullable(msg.getExtra())
                                .map(RxLightChatMessage.ExtraInfo::getLocation)
                                .orElse(null))
                        .build());

    }

    private AppType checkAppType() {
        return AppType.fromValue(this.appType);
    }

    private InputStream loadPayLogo(PaymentChannel paymentChannel) {
        try {
            return getClass().getResourceAsStream("wx_pay_logo.png");
        } catch (Exception e) {
            log.error("Failed to load WeChat Pay logo", e);
            throw new RuntimeException("Failed to load WeChat Pay logo", e);
        }
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
                RedisAPI redis = CDI.current().select(RedisAPI.class).get();
                EventBusPublisher eventBusPublisher = CDI.current().select(EventBusPublisher.class).get();
                this.initBot(redis, eventBusPublisher);
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

    private void initBot(RedisAPI redis, EventBusPublisher eventBusPublisher) throws Exception {
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
                        redis,
                        eventBusPublisher,
                        this.client,
                        this.audioRecognizer,
                        this.translator, false, this.orderApi, AppType.fromValue(this.appType));
                break;
            default:
                break;
        }
    }

    public Runnable startBot(RedisAPI redis, EventBusPublisher eventBusPublisher, Translator translator,
            AudioRecognizer audioRecognizer, ApiEndpoint orderApi) throws Exception {
        this.translator = translator;
        this.audioRecognizer = audioRecognizer;
        this.orderApi = orderApi;
        if (this.client == null) {
            this.init();
        }
        if (this.bot == null) {
            this.initBot(redis, eventBusPublisher);
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
                        if (msg.isPayForBill()) {
                            String payUrl = msg.getCallback().getVariables().get("pay_url").asText();
                            String orderNo = msg.getCallback().getVariables().get("order_no").asText();
                            PaymentChannel paymentChannel = PaymentChannel
                                    .fromString(msg.getCallback().getVariables().get("payment_channel").asText());

                            var image = QRCode.createQRCode(payUrl, 200, 200);
                            QRCode.insertLogo(image, loadPayLogo(paymentChannel), 50, 50, 50, 50);

                            File file = this.fs.resolve(orderNo + ".png").toFile();
                            file.setWritable(true);
                            ImageIO.write(image, "PNG", file);

                            return RxLightChatMessage.builder()
                                    .appId(this.getId())
                                    .userId(msg.getUserId())
                                    .appMessageId(msg.getAppMessageId())
                                    .createdAt(LocalDateTime.now())
                                    .messageType(UserMessageType.IMAGE.getName())
                                    .attachments(Lists2.of(Map.of("file", file, "filename", orderNo + ".png")))
                                    .build();
                        }
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
                    } catch (ErrorResponse e) {
                        switch (BillingErrorCode.fromValue(e.getCode())) {
                            case InsufficientCredit:
                            case CreditPackageExpired:
                                try {
                                    BillingInfo billingInfo = this.client.billing().getBillingInfo();
                                    var variables = JsonUtils.toMap(billingInfo);
                                    variables.put("remaining_credits", billingInfo.getRemainingCredit());
                                    Lists2.foreach(this.client.billing()
                                            .listAvailableCreditPlans("CNY", PaymentChannel.WECHAT).getResults(),
                                            plan -> {
                                                variables.put("packages", JsonUtils.toJsonNode(Lists2.of(Map.of(
                                                        "text", Map.of("tag", "plain_text", "content", plan.getName()),
                                                        "value", plan.getId().toString()))));
                                            });

                                    Callback callback = Callback.builder()
                                            .type(CallbackEventType.PAY_FOR_BILL)
                                            .variables(Maps2.mapValue(variables, JsonUtils::toJsonNode))
                                            .build();
                                    return RxLightChatMessage
                                            .fromChatResponse(ChatResponse.builder()
                                                    .callback(callback)
                                                    .id(msg.getAppMessageId())
                                                    .created(System.currentTimeMillis())
                                                    .object(Constants.CHAT_CALLBACK)
                                                    .build());
                                } catch (Exception ex) {
                                    log.error("Failed to get billing info for appId: {}", this.getId(), ex);
                                    throw new RuntimeException("Failed to get billing info", ex);
                                }
                                // Add other cases as needed
                            default:
                                throw new BizException(e.getCode(), e.getMessage());
                        }
                    } catch (RuntimeException e) {
                        log.error("app {} process message {} occurs error", this.appId, msg.getAppMessageId(), e);
                        if (e instanceof BizException) {
                            if (((BizException) e).getCode() == CommonErrorCode.LocationRequired.getValue()) {
                                return RxLightChatMessage
                                        .builder()
                                        .appId(this.id)
                                        .userId(msg.getUserId())
                                        .content("Location is required")
                                        .messageType(UserMessageType.TEXT.getName())
                                        .createdAt(LocalDateTime.now())
                                        .role(RoleType.ASSISTANT.getName())
                                        .appMessageId(msg.getAppMessageId())
                                        .build();
                            }
                        }
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
