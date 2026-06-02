package cn.ggsn.openrxlight.account.external.impl;

import java.util.Optional;

import org.apache.commons.lang.StringUtils;

import cn.ggsn.openrxlight.account.error.AccountError;
import cn.ggsn.openrxlight.account.external.AuthenticationCodeCache;
import cn.ggsn.openrxlight.account.external.ExternalAccountAuthorizer;
import cn.ggsn.openrxlight.account.external.GetExternalAccountReq;
import cn.ggsn.openrxlight.domain.ExternalAccount;
import cn.ggsn.openrxlight.domain.ExternalAccountType;
import cn.ggsn.openrxlight.errorx.BizException;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor
class PhoneCodeAuthorizer implements ExternalAccountAuthorizer {

    private final AuthenticationCodeCache authenticationCache;

    @Override
    public ExternalAccount authorize(GetExternalAccountReq req) {
        String code = this.authenticationCache.getCode(req.getAccountType(), ExternalAccountType.PHONE,
                req.getExternalAccountId());
        if (StringUtils.isBlank(code)) {
            throw new BizException(AccountError.VerificationCodeExpired, "Authentication code has expired");
        }

        if (!StringUtils.equals(code, req.getAuthCode())) {
            throw new BizException(AccountError.VerificationCodeIncorrect, "Authentication code is incorrect");
        }

        return ExternalAccount.builder()
                .accountType(ExternalAccountType.PHONE)
                .externalAccountId(req.getExternalAccountId())
                .build();
    }

    @Override
    public boolean support(ExternalAccountType accountType) {
        return ExternalAccountType.PHONE.equals(accountType);
    }

    @Override
    public Optional<ExternalAccount> getExternalAccount(GetExternalAccountReq req) {
        return Optional.of(ExternalAccount.builder()
                .accountType(ExternalAccountType.PHONE)
                .externalAccountId(req.getExternalAccountId())
                .build());
    }

}
