package cn.ggsn.openrxlight.services;

import java.util.List;
import java.util.UUID;

import cn.ggsn.openrxlight.config.Config;
import cn.ggsn.openrxlight.httpx.HttpMethod;
import cn.ggsn.openrxlight.request.accounts.CreateAccountRequest;
import cn.ggsn.openrxlight.request.authentication.AcquireAuthRequest;
import cn.ggsn.openrxlight.response.accounts.AccountInfo;
import cn.ggsn.openrxlight.response.authentication.AcquireAuthenticationResponse;
import cn.ggsn.openrxlight.token.GlobalTokenManager;
import cn.ggsn.openrxlight.utils.Transport;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AuthenticationService {
    private final Config config;

    public AcquireAuthenticationResponse acquireAccessToken(AcquireAuthRequest request) throws Exception {
        var resp = Transport.send(this.config, request,
                "/authorization/login",
                HttpMethod.POST.getName(), null)
                .getBody(AcquireAuthenticationResponse.class, this.config);
        GlobalTokenManager.getCache(request.getClientId()).setToken(resp.getAccessToken());
        return resp;
    }

    public AccountInfo createAccount(CreateAccountRequest request) throws Exception {
        return Transport.send(this.config, request,
                "/accounts",
                HttpMethod.POST.getName(), null)
                .getBody(AccountInfo.class, this.config);
    }

    public AccountInfo batchCreateAccount(List<CreateAccountRequest> request) throws Exception {
        return Transport.send(this.config, request,
                "/accounts/batch",
                HttpMethod.POST.getName(), null)
                .getBody(AccountInfo.class, this.config);
    }

    public void deleteAccount(UUID accountId) throws Exception {
        Transport.send(this.config, null,
                String.format("/accounts/%s", accountId),
                HttpMethod.DELETE.getName(), null)
                .getBody(Void.class, this.config);
    }

    public AccountInfo getAccount(UUID accountId, int accountType) throws Exception {
        return Transport.send(this.config, null,
                String.format("/accounts/%d/%s", accountType, accountId),
                HttpMethod.GET.getName(), null)
                .getBody(AccountInfo.class, this.config);
    }
}
