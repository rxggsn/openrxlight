package cn.ggsn.openrxlight.account;

import java.util.Optional;
import java.util.UUID;

import cn.ggsn.openrxlight.account.domain.Account;
import cn.ggsn.openrxlight.account.error.AccountError;
import cn.ggsn.openrxlight.account.external.AuthenticationCodeCache;
import cn.ggsn.openrxlight.account.external.ExternalAccountAuthorizer;
import cn.ggsn.openrxlight.account.external.GetExternalAccountReq;
import cn.ggsn.openrxlight.account.request.AuthorizeRequest;
import cn.ggsn.openrxlight.account.request.LoginRequest;
import cn.ggsn.openrxlight.account.request.VerificationCodeRequest;
import cn.ggsn.openrxlight.account.response.AccountProfile;
import cn.ggsn.openrxlight.account.response.AuthorizeResponse;
import cn.ggsn.openrxlight.account.response.LoginView;
import cn.ggsn.openrxlight.domain.AccountType;
import cn.ggsn.openrxlight.domain.ExternalAccount;
import cn.ggsn.openrxlight.domain.ExternalAccountType;
import cn.ggsn.openrxlight.domain.SourceType;
import cn.ggsn.openrxlight.errorx.BizException;
import cn.ggsn.openrxlight.event.EventBusPublisher;
import cn.ggsn.openrxlight.lang.Lists2;
import cn.ggsn.openrxlight.web.AuthorizationToken;
import cn.ggsn.openrxlight.web.IgnoreAuth;
import cn.ggsn.openrxlight.web.RoleType;
import cn.ggsn.openrxlight.web.BlockingTokenStore;
import io.quarkus.arc.WithCaching;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

@Path("/authorization")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AccountApi {

        private final Instance<ExternalAccountAuthorizer> externalAccountAuthorizers;
        private final AuthenticationCodeCache authenticationCache;
        private final BlockingTokenStore tokenStore;
        private final EventBusPublisher eventBusPublisher;

        public AccountApi(@Any @WithCaching Instance<ExternalAccountAuthorizer> externalAccountAuthorizers,
                        BlockingTokenStore tokenStore, AuthenticationCodeCache authenticationCache,
                        EventBusPublisher eventBusPublisher) {
                this.externalAccountAuthorizers = externalAccountAuthorizers;
                this.tokenStore = tokenStore;
                this.authenticationCache = authenticationCache;
                this.eventBusPublisher = eventBusPublisher;
        }

        @POST
        @Path("/verification-code")
        @IgnoreAuth
        public void getVerificationCode(VerificationCodeRequest request) throws Exception {
                request.validate();
                ExternalAccountType externalAccountType = ExternalAccountType
                                .fromValue(request.getExternalAccountType());
                var acc = Account.getAccountByExternalAccount(request.getExternalAccountId(), externalAccountType,
                                AccountType.fromValue(request.getAccountType()))
                                .orElseGet(() -> {
                                        Account account = new Account("",
                                                        SourceType.fromValue(request.getSourceType()),
                                                        AccountType.fromValue(request.getAccountType()));
                                        account.addExternalAccount(ExternalAccount.builder()
                                                        .externalAccountId(request.getExternalAccountId())
                                                        .accountType(externalAccountType)
                                                        .build());
                                        account.setInActive();

                                        return account;
                                });
                acc.save();
                this.authenticationCache.sendAuthenticationCode(acc, externalAccountType,
                                request.getExternalAccountId());
        }

        @Path("/authorize")
        @POST
        public AuthorizeResponse authorize(AuthorizeRequest request) {
                request.validate();
                ExternalAccountType externalAccountType = ExternalAccountType.fromValue(
                                request.getExternalAccountType());
                AccountType accountType = AccountType.fromValue(request.getAccountType());
                ExternalAccountAuthorizer authorizer = this
                                .selectExternalAccountAuthorizer(externalAccountType)
                                .orElseThrow(() -> new BizException(AccountError.NotSupportExternalAccountType,
                                                externalAccountType.name()));
                ExternalAccount account = authorizer.authorize(GetExternalAccountReq
                                .builder()
                                .externalAccountId(request.getExternalAccountId())
                                .authCode(request.getAuthorizationCode())
                                .externalAccountType(externalAccountType)
                                .accountType(accountType)
                                .build());
                return AuthorizeResponse.builder().externalAccountId(account.getExternalAccountId()).build();
        }

        @Path("/login")
        @POST
        public LoginView login(LoginRequest request) {
                this.externalAccountAuthorizers.forEach(authorizer -> {
                        authorizer.injectCommand(request);
                });
                // Lists2.foreach(this.externalAccountAuthorizers, authorizer -> {
                // authorizer.injectCommand(request);
                // });
                GetExternalAccountReq req = request.toResource();
                SourceType sourceType = req.getSourceType();
                ExternalAccountType externalAccountType = req.getExternalAccountType();
                AccountType accountType = req.getAccountType();
                ExternalAccountAuthorizer authorizer = this
                                .selectExternalAccountAuthorizer(externalAccountType)
                                .orElseThrow(() -> new BizException(
                                                AccountError.NotSupportExternalAccountType,
                                                externalAccountType.name()));

                // Check if the external account exists and the auth code is correct, if not, an
                // exception will be thrown
                var externalAccount = authorizer
                                .getExternalAccount(req)
                                .orElseThrow(() -> new BizException(
                                                AccountError.ExternalAccountNotFound,
                                                req.getExternalAccountId()));
                var account = Account.getAccountByExternalAccount(externalAccount.getExternalAccountId(),
                                externalAccount.getAccountType(), accountType).orElseGet(() -> {
                                        Account acc = new Account("",
                                                        sourceType,
                                                        accountType);
                                        acc.addExternalAccount(externalAccount);
                                        return acc;
                                });
                var token = this.tokenStore.getTokenByAccountId(Optional.ofNullable(account.getAccountId())
                                .map(UUID::toString)
                                .orElse(null),
                                accountType)
                                .orElseGet(() -> {
                                        // if (account.getExternalAccount(
                                        // req.getExternalAccountId(),
                                        // externalAccountType).isEmpty()) {
                                        // account.addExternalAccount(authorizer
                                        // .getExternalAccount(req)
                                        // .orElseThrow(() -> new BizException(
                                        // AccountError.ExternalAccountNotFound,
                                        // req.getExternalAccountId())));
                                        // }
                                        AuthorizationToken authorizationToken = AuthorizationToken
                                                        .builder()
                                                        .accountType(accountType)
                                                        .accountId(account.getAccountId())
                                                        .sourceType(sourceType)
                                                        .roleTypes(Lists2.map(
                                                                        account.getRoleTypes(),
                                                                        type -> RoleType.fromValue(
                                                                                        type)))
                                                        .externalAccountId(req
                                                                        .getExternalAccountId())
                                                        .externalAccountType(
                                                                        externalAccountType)
                                                        .build();
                                        this.tokenStore.setToken(authorizationToken);
                                        return authorizationToken;
                                });
                account.setActive();
                account.refreshLastLoginTime();
                account.setEventBusPublisher(this.eventBusPublisher);
                account.save();
                return LoginView.builder()
                                .accessToken(token.getJwtToken())
                                .expiresIn(token.getExpireTime())
                                .accountId(token.getAccountId().toString())
                                .build();
        }

        private Optional<ExternalAccountAuthorizer> selectExternalAccountAuthorizer(
                        ExternalAccountType accountType) {
                return this.externalAccountAuthorizers.stream()
                                .filter(authorizer -> authorizer.support(accountType))
                                .findFirst();
        }

        @Path("/profile")
        @GET
        public AccountProfile getAccountProfile(@Context SecurityContext securityContext) {
                AuthorizationToken token = (AuthorizationToken) securityContext.getUserPrincipal();
                Account account = Account
                                .getByAccountId(token.getAccountId(), token.getAccountType())
                                .orElseThrow(() -> new BizException(AccountError.AccountNotExist,
                                                "account not exist"));
                return AccountProfile
                                .builder()
                                .accountId(account.getAccountId().toString())
                                .displayName(account.getDisplayName())
                                .avatar(account.getAvatar())
                                .language(account.getLanguage())
                                .accountType(account.getAccountType())
                                .build();
        }
}
