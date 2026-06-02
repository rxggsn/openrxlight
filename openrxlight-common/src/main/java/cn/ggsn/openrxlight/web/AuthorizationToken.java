package cn.ggsn.openrxlight.web;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.fasterxml.jackson.annotation.JsonIgnore;

import cn.ggsn.openrxlight.domain.AccountType;
import cn.ggsn.openrxlight.domain.ExternalAccountType;
import cn.ggsn.openrxlight.domain.SourceType;
import cn.ggsn.openrxlight.errorx.BizException;
import cn.ggsn.openrxlight.errorx.CommonErrorCode;
import cn.ggsn.openrxlight.lang.Lists2;
import jakarta.ws.rs.core.SecurityContext;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

/**
 * @ClassName RxDomainToken
 * @Description RxDomain JWT Token
 * @Author jason.thon
 * @Date 2023/04/19
 * @Version 1.0
 **/
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthorizationToken implements Principal, SecurityContext {
        public static final String BEARER_PREFIX = "Bearer ";
        public static final AuthorizationToken ANONYMOUS = AuthorizationToken.builder()
                        .accountId(UUID.fromString("00000000-0000-0000-0000-000000000000"))
                        .accountType(AccountType.ANONYMOUS)
                        .sourceType(SourceType.UNSPECIFIED)
                        .roleTypes(Lists2.of(RoleType.ANONYMOUS))
                        .externalAccountId("ANONYMOUS")
                        .externalAccountType(ExternalAccountType.ANONYMOUS)
                        .build();
        private static final String SECRET = System.getenv("JWT_SECRET");

        static {
                if (StringUtils.isBlank(SECRET)) {
                        throw new RuntimeException("JWT_SECRET env is not set");
                }
        }

        private UUID accountId;
        private AccountType accountType;
        private String encryptedPassword;
        private SourceType sourceType;
        private List<RoleType> roleTypes;
        @Setter
        private long expireTime;
        private String externalAccountId;
        private ExternalAccountType externalAccountType;

        @SuppressWarnings("null")
        public static AuthorizationToken fromJwtToken(String jwtToken) {
                if (StringUtils.isBlank(jwtToken)) {
                        throw new BizException(CommonErrorCode.InvalidToken);
                }
                Algorithm algorithm = Algorithm.HMAC256(SECRET);
                Map<String, Claim> claims = JWT.require(algorithm).build()
                                .verify(jwtToken).getClaims();
                AuthorizationToken rxDomainToken = new AuthorizationToken();
                rxDomainToken.accountId = UUID.fromString(claims.get("accountId").asString());
                rxDomainToken.accountType = AccountType.fromValue(claims.get("accountType").asInt());
                rxDomainToken.encryptedPassword = claims.get("encryptedPassword") != null
                                ? claims.get("encryptedPassword").asString()
                                : null;
                rxDomainToken.expireTime = claims.get("expireTime").asLong();
                rxDomainToken.sourceType = SourceType.fromValue(claims.get("sourceType").asInt());
                rxDomainToken.roleTypes = Lists2.map(
                                claims.get("roleTypes") != null ? claims.get("roleTypes").asList(Integer.class) : null,
                                RoleType::fromValue);
                rxDomainToken.externalAccountId = claims.get("externalAccountId") != null
                                ? claims.get("externalAccountId").asString()
                                : null;
                rxDomainToken.externalAccountType = ExternalAccountType.fromValue(
                                claims.get("externalAccountType") != null
                                                ? claims.get("externalAccountType").asInt()
                                                : 0);
                return rxDomainToken;
        }

        @JsonIgnore
        public String getJwtToken() {
                Algorithm algorithm = Algorithm.HMAC256(SECRET);
                if (expireTime == 0) {
                        expireTime = LocalDateTime.now().plusDays(1).toEpochSecond(ZoneOffset.UTC);
                }
                return JWT.create().withIssuer("auth0")
                                .withClaim("accountId", accountId.toString())
                                .withClaim("accountType", accountType.getValue())
                                .withClaim("encryptedPassword", encryptedPassword)
                                .withClaim("expireTime", expireTime)
                                .withClaim(
                                                "sourceType",
                                                Optional.ofNullable(sourceType)
                                                                .map(SourceType::getValue).orElse(0))
                                .withClaim("roleTypes", Lists2.map(roleTypes, RoleType::getValue))
                                .withClaim("externalAccountId", externalAccountId)
                                .withClaim("externalAccountType", Optional
                                                .ofNullable(externalAccountType)
                                                .map(ExternalAccountType::getValue)
                                                .orElse(0))
                                .sign(algorithm);
        }

        @JsonIgnore
        public boolean isExpired() {
                return this.expireTime < System.currentTimeMillis() / 1000;
        }

        @Override
        public String getName() {
                return this.accountId.toString();
        }

        @Override
        public Principal getUserPrincipal() {
                return this;
        }

        @Override
        public boolean isUserInRole(String role) {
                return true;
        }

        @Override
        public boolean isSecure() {
                return true;
        }

        @Override
        public String getAuthenticationScheme() {
                return "Bearer";
        }
}
