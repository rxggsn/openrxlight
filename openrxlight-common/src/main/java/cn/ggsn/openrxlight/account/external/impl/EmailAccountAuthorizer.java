package cn.ggsn.openrxlight.account.external.impl;

import java.util.Optional;

import org.apache.commons.lang.StringUtils;

import cn.ggsn.openrxlight.account.domain.Account;
import cn.ggsn.openrxlight.account.domain.EmailAccountInfo;
import cn.ggsn.openrxlight.account.error.AccountError;
import cn.ggsn.openrxlight.account.external.AuthenticationCodeCache;
import cn.ggsn.openrxlight.account.external.ExternalAccountAuthorizer;
import cn.ggsn.openrxlight.account.external.GetExternalAccountReq;
import cn.ggsn.openrxlight.domain.ExternalAccount;
import cn.ggsn.openrxlight.domain.ExternalAccountType;
import cn.ggsn.openrxlight.errorx.BizException;
import cn.ggsn.openrxlight.utils.EncryptUtil;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor
class EmailAccountAuthorizer implements ExternalAccountAuthorizer {

    private final AuthenticationCodeCache authenticationCache;
    private static final String salt = EncryptUtil.genSalt(); // every time application starts, salt is generated to

    @Override
    public ExternalAccount authorize(GetExternalAccountReq req) {
        String code = this.authenticationCache
                .getCode(req.getAccountType(), req.getExternalAccountType(),
                        req.getExternalAccountId());
        if (StringUtils.isBlank(code)) {
            throw new BizException(AccountError.VerificationCodeExpired, "Authentication code has expired");
        }
        if (!StringUtils.equals(code, req.getAuthCode())) {
            throw new BizException(AccountError.VerificationCodeIncorrect, "Authentication code is incorrect");
        }

        Account newAccount = new Account(req.getExternalAccountId(), req.getSourceType(), req.getAccountType());
        ExternalAccount externalAccount = ExternalAccount
                .builder()
                .accountType(ExternalAccountType.EMAIL)
                .externalAccountId(req.getExternalAccountId())
                .accountInfo(EmailAccountInfo.builder()
                        .password(EncryptUtil.bcrypt(req.getAuthCode(), salt))
                        .salt(salt)
                        .build())
                .build();
        newAccount.addExternalAccount(externalAccount);
        newAccount.save();

        externalAccount.setBoundAccountId(newAccount.getAccountId());
        return externalAccount;
    }

    @Override
    public boolean support(ExternalAccountType accountType) {
        return ExternalAccountType.EMAIL.equals(accountType);
    }

    @Override
    public Optional<ExternalAccount> getExternalAccount(GetExternalAccountReq req) {
        ExternalAccount externalAccount = ExternalAccount
                .builder()
                .accountType(ExternalAccountType.EMAIL)
                .externalAccountId(req.getExternalAccountId())
                .build();
        var account = Account
                .getAccountByExternalAccount(externalAccount.getExternalAccountId(),
                        externalAccount.getAccountType(), req.getAccountType())
                .orElseThrow(() -> new BizException(AccountError.AccountNotExist, "account not exist"));
        externalAccount.setBoundAccountId(account.getAccountId());
        if (!Boolean.TRUE.equals(account.getIsActive())) {
            try {
                this.authenticationCache.sendAuthenticationCode(account,
                        req.getExternalAccountType(),
                        req.getExternalAccountId());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            throw new BizException(AccountError.AccountIsNotActive,
                    "account is not active, authentication code has been sent to your email");
        }

        return account
                .getExternalAccount(externalAccount.getExternalAccountId(),
                        externalAccount.getAccountType())
                .map(ea -> {
                    var emailInfo = (EmailAccountInfo) ea.getAccountInfo();
                    if (!EncryptUtil.bcryptCheck(emailInfo.getPassword(), req.getAuthCode(),
                            emailInfo.getSalt())) {
                        throw new BizException(AccountError.PasswordIncorrect, "Password invalid");
                    }

                    externalAccount.setBoundAccountId(account.getAccountId());
                    return externalAccount;
                });
    }

}
