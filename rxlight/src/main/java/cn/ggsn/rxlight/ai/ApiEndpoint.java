package cn.ggsn.rxlight.ai;

import java.time.LocalDateTime;
import java.util.Optional;

import cn.ggsn.openrxlight.account.error.AccountError;
import cn.ggsn.openrxlight.api.OpenRxLightV2;
import cn.ggsn.openrxlight.domain.AccountType;
import cn.ggsn.openrxlight.errorx.BizException;
import cn.ggsn.openrxlight.errorx.CommonErrorCode;
import cn.ggsn.openrxlight.lang.Lists2;
import cn.ggsn.openrxlight.model.chat.RoleType;
import cn.ggsn.openrxlight.request.chat.UserMessageType;
import cn.ggsn.openrxlight.utils.JsonUtils;
import cn.ggsn.openrxlight.utils.MessageIdGenerator;
import cn.ggsn.openrxlight.web.AuthorizationToken;
import cn.ggsn.rxlight.ai.domain.AgentApp;
import cn.ggsn.rxlight.ai.domain.RxLightChatMessage;
import cn.ggsn.rxlight.ai.request.CreateAgentAppRequest;
import cn.ggsn.rxlight.ai.request.RxLightChatRequest;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Path("/ai")
@Slf4j
@RequiredArgsConstructor
public class ApiEndpoint {

    private final OpenRxLightV2 openRxLightV2;
    @Inject
    private Sse sse;

    @POST
    @Path("/chat/completion")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RunOnVirtualThread
    public void chatCompletion(RxLightChatRequest request,
            @Context SseEventSink sink,
            @Context SecurityContext securityContext) throws Exception {
        AuthorizationToken token = (AuthorizationToken) securityContext.getUserPrincipal();
        request.validate();
        // Example implementation: process the chat request and generate a response

        var app = AgentApp
                .getById(request.getAppId())
                .orElseThrow(() -> new BizException(CommonErrorCode.InvalidArgument,
                        String.format("App with ID %d not exist", request.getAppId())));
        app.setClient(this.openRxLightV2);
        String appMessageId = MessageIdGenerator.generate(token.getAccountType(), token.getAccountId(),
                token.getAccountId(),
                this.openRxLightV2.getConfig().getClientSecret(),
                this.openRxLightV2.getConfig().getDataCrypto());
        RxLightChatMessage msg = RxLightChatMessage.builder()
                .appMessageId(request.getId())
                .content(request.getQuery())
                .contextId(request.getContextId())
                .createdAt(LocalDateTime.now())
                .appId(request.getAppId())
                .messageType(request.getMessageType())
                .role(RoleType.USER.getName())
                .userId(token.getAccountId())
                .extra(RxLightChatMessage.ExtraInfo
                        .builder()
                        .location(request.getLocation())
                        .build())
                .build();
        msg.save();

        var finalResult = app.handleMessage(msg).reduce(null, (acc, item) -> {
            if (acc == null) {
                acc = item;
            } else {
                acc.merge(item);
            }
            sink.send(this.sse.newEvent(JsonUtils.toJson(acc)));
            return acc;
        });

        RxLightChatMessage.builder()
                .appMessageId(appMessageId)
                .content(finalResult.getContent().toPrettyString())
                .contextId(finalResult.getContextId())
                .createdAt(LocalDateTime.now())
                .appId(app.getId())
                .openrxlightMessageId(finalResult.getId())
                .messageType(Optional.ofNullable(finalResult.getCallback())
                        .map(callback -> UserMessageType.CALLBACK.getName())
                        .orElseGet(() -> UserMessageType.TEXT.getName()))
                .callback(finalResult.getCallback())
                .role(RoleType.ASSISTANT.getName())
                .build()
                .save();
    }

    @POST
    @Path("/agent-app")
    @Produces(MediaType.APPLICATION_JSON)
    public void createAgentApp(CreateAgentAppRequest request,
            @Context SecurityContext securityContext) {
        AuthorizationToken token = (AuthorizationToken) securityContext.getUserPrincipal();
        if (!AccountType.MERCHANT.equals(token.getAccountType())) {
            throw new BizException(AccountError.PermissionDenied, "Only merchants can create agent apps");
        }

        if (Lists2.anyOf(token.getRoleTypes(), roleType -> !cn.ggsn.openrxlight.web.RoleType.ADMIN.equals(roleType))) {
            throw new BizException(AccountError.PermissionDenied, "Only admins can create agent apps");

        }
        request.validate();
        var app = request.toResource();
        QuarkusTransaction.requiringNew().run(() -> {
            app.persist();
        });
    }
}
