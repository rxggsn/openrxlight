package cn.ggsn.openrxlight.utils;

import java.util.Set;
import java.util.TreeMap;
import org.apache.commons.lang.StringUtils;

import cn.ggsn.openrxlight.Constants;
import cn.ggsn.openrxlight.config.Config;
import cn.ggsn.openrxlight.errorx.ErrorResponse;
import cn.ggsn.openrxlight.httpx.HttpMethod;
import cn.ggsn.openrxlight.httpx.RawRequest;
import cn.ggsn.openrxlight.httpx.RawResponse;
import cn.ggsn.openrxlight.request.OpenRxLightRequest;
import cn.ggsn.openrxlight.request.authentication.AcquireAuthRequest;
import cn.ggsn.openrxlight.response.OpenRxLightResponse;
import cn.ggsn.openrxlight.services.AuthenticationService;
import cn.ggsn.openrxlight.token.GlobalTokenManager;
import cn.ggsn.openrxlight.token.ICache;
import io.reactivex.Flowable;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.net.HttpHeaders;

import okhttp3.HttpUrl;

public class Transport {

    public static final Set<String> IGNORE_AUTH_URL = Sets.newHashSet(
            "/authorization/login");

    public static <Req> OpenRxLightResponse send(
            Config config, Req request, String path, String method,
            TreeMap<String, String> queryParams) throws Exception {
        var body = new OpenRxLightRequest(request, config, path, method);
        ICache cache = GlobalTokenManager.getCache(config.getClientId());
        HttpUrl.Builder urlBuilder = new HttpUrl.Builder()
                .scheme(StringUtils.isNotBlank(config.getScheme()) ? config.getScheme() : Constants.SCHEME)
                .port(config.getPort() > 0 ? config.getPort() : 80)
                .host(StringUtils.isNotBlank(config.getHost()) ? config.getHost() : Constants.BASE_URL);
        if (StringUtils.isNotBlank(config.getBaseUrl())) {
            urlBuilder = urlBuilder.addPathSegments(config.getBaseUrl());
        }
        urlBuilder = urlBuilder.addPathSegments(StringUtils.removeStart(path, "/"));

        if (queryParams != null) {
            for (var entry : queryParams.entrySet()) {
                urlBuilder = urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
            }
            // queryParams.forEach((k, v) -> {
            // urlBuilder.addQueryParameter(k, v);
            // });
        }

        var url = urlBuilder.build();
        var rawReq = RawRequest.builder()
                .body(body)
                .httpMethod(method)
                .reqUrl(url.toString())
                .build();
        String token = cache.getToken();

        if (StringUtils.isBlank(token) && !IGNORE_AUTH_URL.contains(path)) {
            var authenticator = new AuthenticationService(config);
            token = authenticator.acquireAccessToken(AcquireAuthRequest
                    .builder()
                    .clientId(config.getClientId())
                    .clientSecret(config.getClientSecret())
                    .build())
                    .getAccessToken();
            cache.setToken(token);
        }

        if (StringUtils.isNotBlank(token)) {
            rawReq.addHeaders(HttpHeaders.AUTHORIZATION,
                    Lists.newArrayList(StringUtils.join(Lists.newArrayList("Bearer", token), " ")));
        }
        if (StringUtils.equals(method, HttpMethod.GET.getName())
                || StringUtils.equals(method, HttpMethod.DELETE.getName())) {
            rawReq.addHeaders("x-nonce", Lists.newArrayList(body.getNonce()));
            rawReq.addHeaders("x-client-id", Lists.newArrayList(body.getClientId()));
            rawReq.addHeaders("x-signature", Lists.newArrayList(body.getSignature()));
            rawReq.addHeaders("x-timestamp", Lists.newArrayList(Long.toString(body.getTimestamp())));
            rawReq.setBody(null);
        }

        RawResponse execute = config.getHttpTransport().execute(rawReq);
        // if success
        if (execute.getStatusCode() < 300) {
            var resp = JsonUtils.fromJson(execute.getBody(), OpenRxLightResponse.class);
            resp.checkSignature(config, method, path);
            return resp;
        } else {
            // parse error response
            ErrorResponse errorResponse = JsonUtils.fromJson(execute.getBody(), ErrorResponse.class);
            throw errorResponse;
        }

    }

    public static <Resp, Req> Flowable<Resp> sendSse(Config config, Req request, String path, String method,
            Class<Resp> clazz) throws Exception {
        var body = new OpenRxLightRequest(request, config, path, method);
        ICache cache = GlobalTokenManager.getCache(config.getClientId());
        var urlBuilder = new HttpUrl.Builder()
                .scheme(StringUtils.isNotBlank(config.getScheme()) ? config.getScheme() : Constants.SCHEME)
                .host(StringUtils.isNotBlank(config.getHost()) ? config.getHost() : Constants.BASE_URL)
                .port(config.getPort() != -1 ? config.getPort() : 80)
                .addPathSegments(StringUtils.isNotBlank(config.getBaseUrl()) ? config.getBaseUrl() : "")
                .addPathSegments(path);

        var url = urlBuilder.build();
        var rawReq = RawRequest.builder()
                .body(body)
                .httpMethod(method)
                .reqUrl(url.toString())
                .build();
        String token = cache.getToken();
        var encodedPath = url.encodedPath();

        if (StringUtils.isBlank(token) && !IGNORE_AUTH_URL.contains(encodedPath)) {
            var authenticator = new AuthenticationService(config);
            token = authenticator.acquireAccessToken(AcquireAuthRequest
                    .builder()
                    .clientId(config.getClientId())
                    .clientSecret(config.getClientSecret())
                    .build())
                    .getAccessToken();
            cache.setToken(token);
        }

        rawReq.addHeaders(HttpHeaders.AUTHORIZATION, Lists.newArrayList(
                StringUtils.join(Lists.newArrayList("Bearer", token), " ")));
        return config.getHttpTransport().executeSse(rawReq, clazz);
    }

}
