package cn.ggsn.rxlight.ai.agent.impl.lark;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.Heading;
import org.commonmark.node.Node;
import org.commonmark.renderer.text.TextContentRenderer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.lark.oapi.Client;
import com.lark.oapi.event.EventDispatcher;
import com.lark.oapi.service.application.ApplicationService;
import com.lark.oapi.service.application.v6.model.P2BotMenuV6;
import com.lark.oapi.service.cardkit.v1.model.CreateCardReq;
import com.lark.oapi.service.cardkit.v1.model.CreateCardReqBody;
import com.lark.oapi.service.cardkit.v1.model.CreateCardResp;
import com.lark.oapi.service.contact.ContactService.P2UserCreatedV3Handler;
import com.lark.oapi.service.contact.v3.enums.ChildrenDepartmentDepartmentIdTypeEnum;
import com.lark.oapi.service.contact.v3.enums.ChildrenDepartmentUserIdTypeEnum;
import com.lark.oapi.service.contact.v3.enums.FindByDepartmentUserDepartmentIdTypeEnum;
import com.lark.oapi.service.contact.v3.enums.FindByDepartmentUserUserIdTypeEnum;
import com.lark.oapi.service.contact.v3.model.ChildrenDepartmentReq;
import com.lark.oapi.service.contact.v3.model.Department;
import com.lark.oapi.service.contact.v3.model.FindByDepartmentUserReq;
import com.lark.oapi.service.contact.v3.model.P2UserCreatedV3;
import com.lark.oapi.service.im.ImService.P2MessageReceiveV1Handler;
import com.lark.oapi.service.im.v1.enums.CreateImageImageTypeEnum;
import com.lark.oapi.service.im.v1.enums.CreateMessageReceiveIdTypeEnum;
import com.lark.oapi.service.im.v1.enums.FileTypeEnum;
import com.lark.oapi.service.im.v1.model.CreateFileReq;
import com.lark.oapi.service.im.v1.model.CreateFileReqBody;
import com.lark.oapi.service.im.v1.model.CreateImageReq;
import com.lark.oapi.service.im.v1.model.CreateImageReqBody;
import com.lark.oapi.service.im.v1.model.CreateMessageReq;
import com.lark.oapi.service.im.v1.model.CreateMessageReqBody;
import com.lark.oapi.service.im.v1.model.P2MessageReceiveV1;
import com.lark.oapi.service.im.v1.model.ReplyMessageReq;
import com.lark.oapi.service.im.v1.model.ReplyMessageReqBody;
import com.lark.oapi.service.im.v1.model.ReplyMessageResp;

import cn.ggsn.openrxlight.Constants;
import cn.ggsn.openrxlight.account.domain.Account;
import cn.ggsn.openrxlight.account.error.AccountError;
import cn.ggsn.openrxlight.account.event.EventConstants;
import cn.ggsn.openrxlight.api.OpenRxLightV2;
import cn.ggsn.openrxlight.audio.AudioRecognizer;
import cn.ggsn.openrxlight.audio.AudioType;
import cn.ggsn.openrxlight.domain.AccountType;
import cn.ggsn.openrxlight.domain.ExternalAccount;
import cn.ggsn.openrxlight.domain.ExternalAccountType;
import cn.ggsn.openrxlight.domain.SourceType;
import cn.ggsn.openrxlight.errorx.BizException;
import cn.ggsn.openrxlight.event.CloudEvent;
import cn.ggsn.openrxlight.event.ContentType;
import cn.ggsn.openrxlight.event.EventBusPublisher;
import cn.ggsn.openrxlight.event.EventBusType;
import cn.ggsn.openrxlight.lang.Lists2;
import cn.ggsn.openrxlight.lang.Maps2;
import cn.ggsn.openrxlight.model.chat.Callback;
import cn.ggsn.openrxlight.request.chat.UserMessageType;
import cn.ggsn.openrxlight.response.chat.ChatResponse.Attachment;
import cn.ggsn.openrxlight.translator.Translator;
import cn.ggsn.openrxlight.utils.JsonUtils;
import cn.ggsn.openrxlight.web.RoleType;
import cn.ggsn.rxlight.account.event.BatchCreateAccountEvent;
import cn.ggsn.rxlight.ai.agent.AgentBot;
import cn.ggsn.rxlight.ai.agent.impl.lark.card.CallbackVariableTransformer;
import cn.ggsn.rxlight.ai.agent.impl.lark.card.CardActionHandler;
import cn.ggsn.rxlight.ai.agent.impl.lark.card.CardTemplateCache;
import cn.ggsn.rxlight.ai.agent.impl.lark.model.CardMessage;
import cn.ggsn.rxlight.ai.agent.impl.lark.model.FileMessage;
import cn.ggsn.rxlight.ai.agent.impl.lark.model.ImageMessage;
import cn.ggsn.rxlight.ai.agent.impl.lark.model.LocationMessage;
import cn.ggsn.rxlight.ai.agent.impl.lark.model.CardMessage.CardData;
import cn.ggsn.rxlight.ai.agent.impl.lark.model.RichTextPost.PostContent;
import cn.ggsn.rxlight.ai.agent.impl.lark.model.RichTextPost;
import cn.ggsn.rxlight.ai.agent.impl.lark.model.TextMessage;
import cn.ggsn.rxlight.ai.agent.impl.lark.vo.LarkAccountInfo;
import cn.ggsn.rxlight.ai.agent.impl.lark.vo.LarkContext;
import cn.ggsn.rxlight.ai.agent.vo.AgentContext;
import cn.ggsn.rxlight.ai.domain.AppType;
import cn.ggsn.rxlight.ai.domain.RxLightChatMessage;
import cn.ggsn.rxlight.ai.event.CallbackEventType;
import cn.ggsn.rxlight.orders.ApiEndpoint;
import io.vertx.core.Future;
import io.vertx.redis.client.RedisAPI;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LarkBot extends AgentBot {
    private final Client client;
    private final CardTemplateCache cardTemplates;
    private final RedisAPI redis;
    private final EventBusPublisher eventBusPublisher;
    private final AudioRecognizer audioRecognizer;
    private static final String COMMAND_MENU_EVENT = "cmd.menu";
    private final Path fs;
    private final Translator translator;
    private final ApiEndpoint orderApi;

    @RequiredArgsConstructor
    private static class CommandDescription {
        private final Map<String, String> i18nDescriptions;
        @Getter
        private final String icon;

        public String getDescription(String locale) {
            return i18nDescriptions.getOrDefault(locale, i18nDescriptions.get("zh"));
        }
    }

    private static class LarkMessageParser {
        public static RxLightChatMessage parseP2MessageReceiveV1(P2MessageReceiveV1 event, AgentContext context) {
            var eventData = event.getEvent();
            String openId = eventData.getSender().getSenderId().getOpenId();
            var account = (Account) Account
                    .getAccountByExternalAccount(openId, ExternalAccountType.FEISHU,
                            AppType.FEISHU.equals(context.getAppType()) ? AccountType.OPERATOR
                                    : AccountType.CONSUMER)
                    .orElseThrow(() -> new BizException(AccountError.AccountNotExist, openId));

            // var account = Account.getAccountByExternalAccount(openId,
            // ExternalAccountType.FEISHU, AccountType.OPERATOR)
            // .orElseThrow(() -> new BizException(AccountError.AccountNotExist, openId));
            var developerAccount = account.getExternalAccounts(Lists2.of(ExternalAccountType.DEVELOPER)).stream()
                    .findFirst()
                    .orElseThrow(() -> new BizException(AccountError.NotSupportExternalAccountType,
                            ExternalAccountType.DEVELOPER.name()));
            UserMessageType messageType = UserMessageType.fromName(event.getEvent().getMessage().getMessageType());

            RxLightChatMessage rxLightChatMessage = RxLightChatMessage.builder()
                    .appMessageId(eventData.getMessage().getMessageId())
                    .messageType(eventData.getMessage().getMessageType())
                    .userId(account.getAccountId())
                    .contextId(eventData.getMessage().getThreadId())
                    .openrxlightAccountId(developerAccount.getExternalAccountId())
                    .build();
            switch (messageType) {
                case AUDIO:
                    rxLightChatMessage.setContent(
                            JsonUtils.fromJson(eventData.getMessage().getContent(), FileMessage.class).getFileKey());
                case IMAGE:
                case POST:
                case FILE:
                case INTERACTIVE:
                    throw new IllegalArgumentException("Only supported [text] type message");
                case LOCATION:
                    LocationMessage locationMessage = JsonUtils.fromJson(eventData.getMessage().getContent(),
                            LocationMessage.class);
                    rxLightChatMessage.setMessageType(UserMessageType.LOCATION.getName());
                    rxLightChatMessage.setContent(StringUtils.join(Lists2.of(locationMessage.getLatitude(),
                            locationMessage.getLongitude()), ","));
                    rxLightChatMessage.setExtra(RxLightChatMessage.ExtraInfo.builder()
                            .location(StringUtils.join(Lists2.of(locationMessage.getLatitude(),
                                    locationMessage.getLongitude()), ","))
                            .build());
                    break;
                default:
                    rxLightChatMessage.setContent(
                            JsonUtils.fromJson(eventData.getMessage().getContent(), TextMessage.class).getText());
                    break;
            }

            return rxLightChatMessage;
        }

    }

    public LarkBot(int agentId,
            String appId,
            String appSecret,
            String verificationToken,
            String encryptKey,
            RedisAPI redis,
            EventBusPublisher eventBusPublisher,
            OpenRxLightV2 openRxLightV2,
            AudioRecognizer audioRecognizer,
            Translator translator,
            Boolean offline, ApiEndpoint orderApi,
            AppType appType)
            throws IOException {
        super(new LarkContext(redis, agentId, appId, appType));
        this.client = Client.newBuilder(appId, appSecret).build();

        this.cardTemplates = new CardTemplateCache("/thirdparty/lark/templates/templates.json");
        this.redis = redis;
        this.eventBusPublisher = eventBusPublisher;
        this.audioRecognizer = audioRecognizer;
        this.translator = translator;
        this.orderApi = orderApi;
        try {
            this.fs = Path.of(System.getProperty("user.home"), ".rxlight", "app_files", String.valueOf(agentId));
            this.fs.toFile().mkdirs();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize file system", e);
        }

        Thread.ofVirtual().start(() -> {
            log.info("Initializing LarkBot");
            this.initializeAccounts();
            log.info("LarkBot initialized successfully");
        });

        if (Boolean.TRUE.equals(offline)) {
            log.debug("LarkBot is running in offline mode, skipping websocket initialization");
            return;
        }

        final Map<String, CommandDescription> commandDescriptions = Map.ofEntries(
                Maps.immutableEntry("create_device", new CommandDescription(Map.ofEntries(
                        Maps.immutableEntry("zh", "创建设备"),
                        Maps.immutableEntry("en", "Create Device")), "qr_outlined")),
                Maps.immutableEntry("create_station", new CommandDescription(Map.ofEntries(
                        Maps.immutableEntry("zh", "创建站点"),
                        Maps.immutableEntry("en", "Create Station")), "pin_outlined")),
                Maps.immutableEntry("update_device", new CommandDescription(Map.ofEntries(
                        Maps.immutableEntry("zh", "更新设备"),
                        Maps.immutableEntry("en", "Update Device")), "scan_outlined")),
                Maps.immutableEntry("update_station", new CommandDescription(Map.ofEntries(
                        Maps.immutableEntry("zh", "更新站点"),
                        Maps.immutableEntry("en", "Update Station")), "setting-inter_outlined")));

        new com.lark.oapi.ws.Client.Builder(appId, appSecret)
                .autoReconnect(true)
                .eventHandler(EventDispatcher
                        .newBuilder(verificationToken, encryptKey)
                        .onP2MessageReceiveV1(new P2MessageReceiveV1Handler() {
                            @Override
                            public void handle(P2MessageReceiveV1 event) throws Exception {
                                if (!context.isDuplicated(event.getEvent().getMessage().getMessageId())) {
                                    push(LarkMessageParser.parseP2MessageReceiveV1(event, context));
                                }
                            }
                        })
                        .onP2BotMenuV6(new ApplicationService.P2BotMenuV6Handler() {
                            @Override
                            public void handle(P2BotMenuV6 event) throws Exception {
                                var account = Account.getAccountByExternalAccount(
                                        event.getEvent().getOperator().getOperatorId().getOpenId(),
                                        ExternalAccountType.FEISHU,
                                        AppType.FEISHU.equals(context.getAppType()) ? AccountType.OPERATOR
                                                : AccountType.CONSUMER)
                                        .orElseThrow(() -> new BizException(AccountError.AccountNotExist,
                                                event.getEvent().getOperator().getOperatorId().getOpenId()));
                                var openRxLightAccounts = account
                                        .getExternalAccounts(Lists2.of(ExternalAccountType.DEVELOPER));
                                if (COMMAND_MENU_EVENT.equals(event.getEvent().getEventKey())) {
                                    openRxLightAccounts.stream().findFirst().ifPresent(externalAccount -> {
                                        try {
                                            var accountInfo = openRxLightV2.authentication()
                                                    .getAccount(
                                                            UUID.fromString(externalAccount.getExternalAccountId()),
                                                            account.getAccountType());
                                            JsonNodeFactory instance = JsonNodeFactory.instance;
                                            var commands = instance.arrayNode().addAll(Lists2.map(
                                                    accountInfo.getCommands(),
                                                    command -> instance.objectNode().put("text",
                                                            commandDescriptions.get(command)
                                                                    .getDescription(account.getLanguage()))
                                                            .put("value", command).replace("icon",
                                                                    instance.objectNode().put("token",
                                                                            commandDescriptions.get(command)
                                                                                    .getIcon())
                                                                            .put("tag", "standard_icon"))));
                                            createMessage(RxLightChatMessage
                                                    .builder()
                                                    .userId(account.getAccountId())
                                                    .messageType(UserMessageType.CALLBACK.getName())
                                                    .callback(Callback.builder().type(CallbackEventType.CUSTOM_CMD)
                                                            .variables(Maps2.of("commands", commands))
                                                            .build())
                                                    .build());
                                        } catch (Exception e) {
                                            log.error("Failed to handle command menu event for account {}, error: {}",
                                                    account.getAccountId(), e.getMessage(), e);
                                        }
                                    });
                                }
                            }

                        })
                        .onP2CardActionTrigger(
                                new CardActionHandler(this.recvQueue, client, (LarkContext) this.context, openRxLightV2,
                                        this.cardTemplates,
                                        this.fs, this.orderApi))
                        .onP2UserCreatedV3(new P2UserCreatedV3Handler() {
                            @Override
                            public void handle(P2UserCreatedV3 event) throws Exception {
                                var user = event.getEvent().getObject();
                                var account = Account
                                        .getAccountByExternalAccount(user.getOpenId(), ExternalAccountType.FEISHU,
                                                AppType.FEISHU.equals(context.getAppType()) ? AccountType.OPERATOR
                                                        : AccountType.CONSUMER)
                                        .orElseGet(() -> {
                                            Account acc = new Account(user.getName(), SourceType.PARTNER,
                                                    AppType.FEISHU.equals(context.getAppType()) ? AccountType.OPERATOR
                                                            : AccountType.CONSUMER);
                                            acc.addExternalAccount(ExternalAccount.builder()
                                                    .externalAccountId(user.getOpenId())
                                                    .accountType(ExternalAccountType.FEISHU)
                                                    .build());
                                            if (Boolean.TRUE.equals(user.getIsTenantManager())) {
                                                acc.addRoleType(RoleType.ADMIN);
                                            } else {
                                                acc.addRoleType(
                                                        AppType.FEISHU.equals(context.getAppType()) ? RoleType.OPERATOR
                                                                : RoleType.GUEST);
                                            }
                                            if (StringUtils.isNotBlank(user.getMobile())) {
                                                acc.addExternalAccount(ExternalAccount.builder()
                                                        .externalAccountId(user.getMobile())
                                                        .accountType(ExternalAccountType.PHONE)
                                                        .build());
                                            }
                                            if (StringUtils.isNotBlank(user.getEmail())) {
                                                acc.addExternalAccount(ExternalAccount.builder()
                                                        .externalAccountId(user.getEmail())
                                                        .accountType(ExternalAccountType.EMAIL)
                                                        .build());
                                            }
                                            return acc;
                                        });
                                if (account.id == null || account.id == 0) {
                                    account.save();
                                }
                            }

                        })
                        .build())
                .build()
                .start();
    }

    private void initializeAccounts() {
        String syncFlag = String.format("claw:lark:accounts:sync:%s:%d:flag", this.context.getAppId(),
                this.context.getAppType().getCode());
        var response = Future
                .await(this.redis.get(syncFlag));
        var completed = Optional
                .ofNullable(response)
                .map(resp -> "1".equals(resp.toString()))
                .orElse(Boolean.FALSE);

        if (Boolean.TRUE.equals(completed)) {
            log.info("Accounts have already been synchronized from Lark, skipping initialization");
            return;
        }
        try {
            int pageSize = 50;

            Queue<Department> departments = Queues.newArrayDeque();
            departments.add(Department.newBuilder().departmentId("0").openDepartmentId("0").build());

            List<Account> accounts = Lists2.empty();
            Set<String> existOpenIds = Sets.newHashSet();

            while (!departments.isEmpty()) {
                Department department = departments.poll();
                if (StringUtils.isBlank(department.getOpenDepartmentId())) {
                    continue;
                }
                String listDepartmentPageToken = null;
                Boolean subDepartmentsHasMore = true;
                Boolean userHasMore = true;
                String userPageToken = null;
                while (userHasMore) {
                    var userResp = this.client.contact()
                            .v3()
                            .user()
                            .findByDepartment(FindByDepartmentUserReq
                                    .newBuilder()
                                    .departmentId(department.getOpenDepartmentId())
                                    .departmentIdType(
                                            FindByDepartmentUserDepartmentIdTypeEnum.OPEN_DEPARTMENT_ID)
                                    .userIdType(FindByDepartmentUserUserIdTypeEnum.OPEN_ID)
                                    .pageSize(pageSize)
                                    .pageToken(userPageToken)
                                    .build());
                    if (!userResp.success()) {
                        log.error("Failed to fetch users for department {} from Lark: {}",
                                department.getOpenDepartmentId(),
                                userResp.getMsg());
                        return;
                    }

                    if (userResp.getData().getItems() == null) {
                        break;
                    }
                    for (var user : userResp.getData().getItems()) {
                        if (Boolean.TRUE.equals(user.getIsFrozen())) {
                            continue;
                        }
                        var account = Account
                                .getAccountByExternalAccount(user.getOpenId(),
                                        ExternalAccountType.FEISHU,
                                        AppType.FEISHU.equals(this.context.getAppType()) ? AccountType.OPERATOR
                                                : AccountType.CONSUMER)
                                .orElseGet(() -> {
                                    Account acc = new Account(user.getName(),
                                            SourceType.PARTNER,
                                            AppType.FEISHU.equals(this.context.getAppType()) ? AccountType.OPERATOR
                                                    : AccountType.CONSUMER);
                                    acc.setAccountId(cn.ggsn.openrxlight.lang.UUID.randomUUID());
                                    acc.addExternalAccount(ExternalAccount.builder()
                                            .externalAccountId(user.getOpenId())
                                            .accountType(ExternalAccountType.FEISHU)
                                            .accountInfo(LarkAccountInfo.builder()
                                                    .openId(user.getOpenId())
                                                    .unionId(user.getUnionId())
                                                    .userId(user.getUserId())
                                                    .appId(this.context.getAppId())
                                                    .build())
                                            .build());
                                    if (Boolean.TRUE.equals(user.getIsTenantManager())) {
                                        acc.addRoleType(RoleType.ADMIN);
                                    } else {
                                        acc.addRoleType(
                                                AppType.FEISHU.equals(this.context.getAppType()) ? RoleType.OPERATOR
                                                        : RoleType.GUEST);
                                    }
                                    if (StringUtils.isNotBlank(user.getMobile())) {
                                        acc.addExternalAccount(ExternalAccount.builder()
                                                .externalAccountId(user.getMobile())
                                                .accountType(ExternalAccountType.PHONE)
                                                .build());
                                    }
                                    if (StringUtils.isNotBlank(user.getEmail())) {
                                        acc.addExternalAccount(ExternalAccount.builder()
                                                .externalAccountId(user.getEmail())
                                                .accountType(ExternalAccountType.EMAIL)
                                                .build());
                                    }
                                    return acc;
                                });
                        if (account.id == null || account.id == 0) {
                            if (!existOpenIds.contains(user.getOpenId())) {
                                accounts.add(account);
                                existOpenIds.add(user.getOpenId());
                            }
                        }
                    }

                    userHasMore = userResp.getData().getHasMore();
                    userPageToken = userResp.getData().getPageToken();
                }

                while (subDepartmentsHasMore) {
                    var subDepartmentResp = this.client
                            .contact()
                            .v3()
                            .department()
                            .children(ChildrenDepartmentReq
                                    .newBuilder()
                                    .departmentIdType(ChildrenDepartmentDepartmentIdTypeEnum.OPEN_DEPARTMENT_ID)
                                    .departmentId(department.getOpenDepartmentId())
                                    .userIdType(ChildrenDepartmentUserIdTypeEnum.OPEN_ID)
                                    .pageSize(pageSize)
                                    .pageToken(listDepartmentPageToken)
                                    .build());
                    if (!subDepartmentResp.success()) {
                        log.error("Failed to fetch departments from Lark: {}",
                                subDepartmentResp.getMsg());
                        return;
                    }

                    listDepartmentPageToken = subDepartmentResp.getData().getPageToken();
                    subDepartmentsHasMore = subDepartmentResp.getData().getHasMore();

                    if (subDepartmentResp.getData().getItems() != null) {
                        for (Department items : subDepartmentResp.getData().getItems()) {
                            departments.offer(items);
                        }
                    }
                }

            }

            var event = BatchCreateAccountEvent
                    .builder()
                    .accounts(Lists2.map(accounts, account -> {
                        account.save();
                        return account;
                    }))
                    .build();
            CloudEvent<BatchCreateAccountEvent> e = new CloudEvent<>(null, null,
                    EventConstants.AccountEvent.ACCOUNT_CREATED,
                    EventConstants.AccountEvent.ACCOUNT_TOPIC, ContentType.APPLICATION_JSON);
            e.setData(event);
            this.eventBusPublisher.publish(e, EventBusType.LOCAL);
        } catch (Exception e) {
            log.error("Failed to initialize accounts from Lark", e);
        }

        Future.await(this.redis.set(Lists2.of(syncFlag, "1"))
                .map(r -> null)
                .onFailure(ex -> {
                    log.error("Failed to set sync flag in Redis", ex);
                }));
    }

    @Override
    public void send(RxLightChatMessage msg) throws Exception {
        if (StringUtils.isBlank(msg.getContent()) && msg.getCallback() == null) {
            return;
        }
        UserMessageType messageType = UserMessageType.fromName(msg.getMessageType());
        if (Lists2.isNotEmpty(msg.getAttachments())) {
            switch (messageType) {
                case AUDIO:
                    String text = msg.getContent();
                    String i18n = this.translator.getI18n(text);
                    String filename = String.format("%d/%s/%s.opus", msg.getAppId(), msg.getAppMessageId(),
                            LocalDateTime.now().format(Constants.QUERY_DATE_TIME_FORMATTER));
                    File file = this.fs.resolve(filename).toFile();
                    try (OutputStream os = new FileOutputStream(file)) {
                        this.audioRecognizer.textToSpeech(new AudioRecognizer.TextToSpeechRequest(
                                text, AudioType.OPUS, i18n, 16000, os));
                    }
                    msg.getAttachments().add(Map.of("file", file, "filename", filename));
                    break;
                case IMAGE:
                case FILE:
                    break;
                default:
                    List<Attachment> attachments = msg.getAttachmentObjects();

                    var links = Lists2.map(attachments, attachment -> {
                        return String.format("[%s](%s)", attachment.getFilename(), attachment.getFileId());
                    });

                    msg.setContent(String.format("%s\n\n%s", msg.getContent(), StringUtils.join(links, "\n")));
                    break;
            }

        }
        if (StringUtils.isNotBlank(msg.getContextId())) {
            this.replyMessage(msg);
        } else {
            this.createMessage(msg);
        }
    }

    private void createMessage(RxLightChatMessage msg) throws Exception {
        for (var req : transformToCreateMessageReq(msg)) {
            var resp = this.client.im().v1().message().create(
                    CreateMessageReq.newBuilder()
                            .receiveIdType(CreateMessageReceiveIdTypeEnum.OPEN_ID)
                            .createMessageReqBody(req)
                            .build());
            if (!resp.success()) {
                log.error("Failed to send message: {}. error message: {}, details: {}", msg.getAppMessageId(),
                        resp.getMsg(), JsonUtils.toJson(resp.getError().getDetails()));
            } else {
                this.context.putMsgIndex(msg.getAppMessageId(), resp.getData().getMessageId());
                log.info("Successfully sent message: {} to user: {}", msg.getAppMessageId(), msg.getUserId());
            }
        }
    }

    private void replyMessage(RxLightChatMessage msg) throws Exception {
        for (var request : transformToReplyMessageReq(msg)) {
            ReplyMessageReq req = ReplyMessageReq.newBuilder()
                    .messageId(msg.getAppMessageId())
                    .replyMessageReqBody(request)
                    .build();

            ReplyMessageResp resp = this.client.im().v1().message().reply(req);
            if (!resp.success()) {
                log.error("Failed to reply message: {}. error message: {}, details: {}", msg.getAppMessageId(),
                        resp.getMsg(), JsonUtils.toJson(resp.getError().getDetails()));
            } else {
                this.context.putMsgIndex(msg.getAppMessageId(), resp.getData().getMessageId());
                log.info("Successfully replied message: {} to user: {}", msg.getAppMessageId(), msg.getUserId());
            }
        }

    }

    private List<ReplyMessageReqBody> transformToReplyMessageReq(RxLightChatMessage msg) throws Exception {
        UserMessageType messageType = UserMessageType.fromName(msg.getMessageType());
        switch (messageType) {
            case CALLBACK:
                var template = this.cardTemplates.getTemplate(msg.getCallback().getType());
                if (template == null) {
                    template = this.cardTemplates.getTemplate("default");
                }

                template.replaceVariables(msg.getCallback().getVariables(), null);
                String cardJson = JsonUtils.toJson(template.getContent());
                var card = this.createCard(cardJson);

                return Lists2.of(ReplyMessageReqBody.newBuilder()
                        .content(JsonUtils.toJson(CardMessage.builder()
                                .data(CardData.builder().cardId(card.getData().getCardId()).build())
                                .build()))
                        .msgType(UserMessageType.INTERACTIVE.getName())
                        .replyInThread(true)
                        .build());
            case IMAGE:
            case AUDIO:
            case FILE:
                return Lists2.mapNotNull(msg.getAttachmentObjects(), attachment -> {
                    // in k8s environment, the file system can be mounted to a shared storage like
                    // NFS, S3, etc.
                    var file = attachment.downloadToFs(this.fs);
                    try {
                        var fileKey = this.uploadFileToLark(file, messageType);
                        switch (messageType) {
                            case IMAGE:
                                return ReplyMessageReqBody.newBuilder()
                                        .content(JsonUtils.toJson(new ImageMessage(fileKey)))
                                        .msgType(UserMessageType.IMAGE.getName())
                                        .replyInThread(true)
                                        .build();
                            case AUDIO:
                            case FILE:
                                return ReplyMessageReqBody.newBuilder()
                                        .content(JsonUtils.toJson(new FileMessage(fileKey)))
                                        .msgType(UserMessageType.FILE.getName())
                                        .replyInThread(true)
                                        .build();
                            default:
                                return null;
                        }
                    } catch (Exception e) {
                        log.error("Failed to upload file to Lark for file: {}", attachment.getFilename(), e);
                        throw new RuntimeException(
                                String.format("Failed to upload file to Lark for file: %s", attachment.getFilename()),
                                e);
                    } finally {
                        if (file.exists()) {
                            file.delete();
                        }
                    }

                });
            default:
                org.commonmark.parser.Parser parser = org.commonmark.parser.Parser.builder().build();
                List<String> titles = Lists2.empty();
                Node node = parser.parse(msg.getContent());
                node.accept(new AbstractVisitor() {
                    @Override
                    public void visit(Heading heading) {
                        if (heading.getLevel() == 1) {
                            titles.add(TextContentRenderer.builder().build().render(heading));
                        }
                    }
                });

                RichTextPost post = RichTextPost.builder()
                        .title(Lists2.isNotEmpty(titles) ? titles.get(0) : "")
                        .content(Lists2.of(Lists2.of(new PostContent("md", msg.getContent()))))
                        .build();

                return Lists2.of(ReplyMessageReqBody.newBuilder()
                        .content(JsonUtils.toJson(Map.of("zh_cn", post)))
                        .msgType(UserMessageType.POST.getName())
                        .replyInThread(true)
                        .build());
        }
    }

    private List<CreateMessageReqBody> transformToCreateMessageReq(RxLightChatMessage msg) throws Exception {
        var feishuAccount = Account
                .getByAccountId(msg.getUserId(), AppType.FEISHU.equals(context.getAppType()) ? AccountType.OPERATOR
                        : AccountType.CONSUMER)
                .orElseThrow(() -> new BizException(AccountError.AccountNotExist, msg.getUserId().toString()))
                .getExternalAccounts(Lists2.of(ExternalAccountType.FEISHU)).stream().findFirst()
                .orElseThrow(() -> new BizException(AccountError.NotSupportExternalAccountType,
                        ExternalAccountType.FEISHU.name()));
        UserMessageType messageType = UserMessageType.fromName(msg.getMessageType());
        switch (messageType) {
            case CALLBACK:
                var callback = CallbackVariableTransformer.transform(msg.getCallback());
                var template = this.cardTemplates.getTemplate(callback.getType());
                if (template == null) {
                    template = this.cardTemplates.getTemplate("default");
                }

                template.replaceVariables(callback.getVariables(), null);
                template.replaceVariables(Maps2.of("callback_id",
                        ((JsonNode) JsonNodeFactory.instance.textNode(callback.getCallbackId()))), null);
                String cardJson = JsonUtils.toJson(template.getContent());
                var card = this.createCard(cardJson);
                ((LarkContext) this.context).putCardIndex(msg.getAppMessageId(),
                        card.getData().getCardId());

                return Lists2.of(CreateMessageReqBody.newBuilder()
                        .content(JsonUtils.toJson(CardMessage.builder()
                                .type("card")
                                .data(CardData.builder().cardId(card.getData().getCardId()).build())
                                .build()))
                        .msgType(UserMessageType.INTERACTIVE.getName())
                        .receiveId(feishuAccount.getExternalAccountId())
                        .build());
            case IMAGE:
            case AUDIO:
            case FILE:
                return Lists2.mapNotNull(msg.getAttachmentObjects(), attachment -> {
                    var file = attachment.downloadToFs(this.fs);
                    try {
                        var fileKey = this.uploadFileToLark(file, messageType);
                        switch (messageType) {
                            case IMAGE:
                                return CreateMessageReqBody.newBuilder()
                                        .content(JsonUtils.toJson(new ImageMessage(fileKey)))
                                        .msgType(UserMessageType.IMAGE.getName())
                                        .receiveId(feishuAccount.getExternalAccountId())
                                        .build();
                            case AUDIO:
                            case FILE:
                                return CreateMessageReqBody.newBuilder()
                                        .content(JsonUtils.toJson(new FileMessage(fileKey)))
                                        .msgType(UserMessageType.FILE.getName())
                                        .receiveId(feishuAccount.getExternalAccountId())
                                        .build();
                            default:
                                return null;
                        }
                    } catch (Exception e) {
                        log.error("Failed to upload file to Lark for file: {}", attachment.getFilename(), e);
                        throw new RuntimeException(
                                String.format("Failed to upload file to Lark for file: %s", attachment.getFilename()),
                                e);
                    } finally {
                        if (file.exists()) {
                            file.delete();
                        }
                    }

                });
            case TEXT:
                TextMessage textMessage = TextMessage.builder().text(msg.getContent()).build();
                return Lists2.of(CreateMessageReqBody.newBuilder()
                        .content(JsonUtils.toJson(textMessage))
                        .msgType(UserMessageType.TEXT.getName())
                        .receiveId(feishuAccount.getExternalAccountId())
                        .build());
            default:
                org.commonmark.parser.Parser parser = org.commonmark.parser.Parser.builder().build();
                List<String> titles = Lists2.empty();
                Node node = parser.parse(msg.getContent());
                node.accept(new AbstractVisitor() {
                    @Override
                    public void visit(Heading heading) {
                        if (heading.getLevel() == 1) {
                            titles.add(TextContentRenderer.builder().build().render(heading));
                        }
                    }
                });

                RichTextPost post = RichTextPost.builder()
                        .title(Lists2.isNotEmpty(titles) ? titles.get(0) : "")
                        .content(Lists2.of(Lists2.of(new PostContent("md", msg.getContent()))))
                        .build();

                return Lists2.of(CreateMessageReqBody.newBuilder()
                        .content(JsonUtils.toJson(Map.of("zh_cn", post)))
                        .msgType(UserMessageType.POST.getName())
                        .receiveId(feishuAccount.getExternalAccountId())
                        .build());
        }
    }

    private String uploadFileToLark(File file, UserMessageType messageType) throws Exception {
        if (UserMessageType.IMAGE.equals(messageType)) {
            var resp = this.client.im().v1().image().create(
                    CreateImageReq.newBuilder()
                            .createImageReqBody(CreateImageReqBody.newBuilder()
                                    .image(file)
                                    .imageType(CreateImageImageTypeEnum.MESSAGE)
                                    .build())
                            .build());
            if (!resp.success()) {
                log.error("Failed to upload image to Lark for file: {}, error message: {}, details: {}",
                        file.getName(), resp.getMsg(), JsonUtils.toJson(resp.getError().getDetails()));
                throw new RuntimeException(
                        String.format("Failed to upload image to Lark for file: %s, error message: %s, details: %s",
                                file.getName(), resp.getMsg(), JsonUtils.toJson(resp.getError().getDetails())));
            } else {
                return resp.getData().getImageKey();
            }
        } else if (UserMessageType.FILE.equals(messageType) || UserMessageType.AUDIO.equals(messageType)) {
            var resp = this.client.im().v1().file().create(
                    CreateFileReq.newBuilder()
                            .createFileReqBody(CreateFileReqBody.newBuilder()
                                    .file(file)
                                    .fileType(getFileTypeFromName(file).getValue())
                                    .build())
                            .build());
            if (!resp.success()) {
                log.error("Failed to upload file to Lark for file: {}, error message: {}, details: {}",
                        file.getName(), resp.getMsg(), JsonUtils.toJson(resp.getError().getDetails()));
                throw new RuntimeException(
                        String.format("Failed to upload file to Lark for file: %s, error message: %s, details: %s",
                                file.getName(), resp.getMsg(), JsonUtils.toJson(resp.getError().getDetails())));
            } else {
                return resp.getData().getFileKey();
            }
        } else {
            throw new IllegalArgumentException(String.format("Unsupported message type for file upload: %s",
                    messageType));
        }
    }

    public CreateCardResp createCard(String cardJson) throws Exception {
        if (StringUtils.isNotBlank(cardJson)) {
            cardJson = StringEscapeUtils.unescapeJava(cardJson);
        }

        CreateCardReq req = CreateCardReq.newBuilder()
                .createCardReqBody(CreateCardReqBody.newBuilder()
                        .type("card_json")
                        .data(cardJson)
                        .build())
                .build();
        CreateCardResp resp = this.client.cardkit().v1().card().create(req);

        if (!resp.success()) {
            log.error("Failed to create card because: {}", resp);
            throw new RuntimeException(String.format("Failed to create card because: %s", resp.getMsg()));
        }

        return resp;
    }

    private static FileTypeEnum getFileTypeFromName(File file) {
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1 || dotIndex == fileName.length() - 1) {
            return FileTypeEnum.STREAM;
        }
        String extension = fileName.substring(dotIndex + 1).toLowerCase();
        switch (extension) {
            case "mp4":
                return FileTypeEnum.MP4;
            case "opus":
                return FileTypeEnum.OPUS;
            case "pdf":
                return FileTypeEnum.PDF;
            case "doc":
            case "docx":
                return FileTypeEnum.DOC;
            case "xls":
            case "xlsx":
                return FileTypeEnum.XLS;
            case "ppt":
            case "pptx":
                return FileTypeEnum.PPT;
            default:
                return FileTypeEnum.STREAM;
        }
    }
}
