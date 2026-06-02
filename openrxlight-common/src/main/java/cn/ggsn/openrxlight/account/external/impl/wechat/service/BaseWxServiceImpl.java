package cn.ggsn.openrxlight.account.external.impl.wechat.service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import org.apache.commons.lang.StringUtils;

import cn.ggsn.openrxlight.account.external.impl.wechat.error.WxError;
import cn.ggsn.openrxlight.account.external.impl.wechat.error.WxErrorException;
import cn.ggsn.openrxlight.account.external.impl.wechat.error.WxRuntimeException;
import cn.ggsn.openrxlight.account.external.impl.wechat.model.WxAccessToken;
import cn.ggsn.openrxlight.account.external.impl.wechat.model.WxConfig;
import cn.ggsn.openrxlight.account.external.impl.wechat.model.WxType;
import cn.ggsn.openrxlight.account.external.impl.wechat.request.RequestExecutor;
import cn.ggsn.openrxlight.account.external.impl.wechat.request.RequestHttp;
import cn.ggsn.openrxlight.account.external.impl.wechat.request.SimpleGetRequestExecutor;
import cn.ggsn.openrxlight.account.external.impl.wechat.request.SimplePostRequestExecutor;
import cn.ggsn.openrxlight.account.external.impl.wechat.utils.DataUtils;
import cn.ggsn.openrxlight.account.external.impl.wechat.utils.WxConsts;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public abstract class BaseWxServiceImpl<H, P> implements RequestHttp<H, P> {
    @Getter(AccessLevel.PROTECTED)
    private final WxConfig wxMaConfig;
    private int retrySleepMillis = 1000;
    private int maxRetryTimes = 5;

    /**
     * 向微信端发送请求，在这里执行的策略是当发生access_token过期时才去刷新，然后重新执行请求，而不是全局定时请求
     */
    protected <T, E> T execute(RequestExecutor<T, E> executor, String uri, E data) throws WxErrorException {
        int retryTimes = 0;
        do {
            try {
                return this.executeInternal(executor, uri, data, false);
            } catch (WxErrorException e) {
                if (retryTimes + 1 > this.maxRetryTimes) {
                    log.warn("重试达到最大次数【{}】", maxRetryTimes);
                    // 最后一次重试失败后，直接抛出异常，不再等待
                    throw new WxErrorException(WxError.builder()
                            .errorCode(e.getError().getErrorCode())
                            .errorMsg("微信服务端异常，超出重试次数！")
                            .build());
                }

                WxError error = e.getError();
                // -1 系统繁忙, 1000ms后重试
                if (error.getErrorCode() == -1) {
                    int sleepMillis = this.retrySleepMillis * (1 << retryTimes);
                    try {
                        log.warn("微信系统繁忙，{} ms 后重试(第{}次)", sleepMillis, retryTimes + 1);
                        Thread.sleep(sleepMillis);
                    } catch (InterruptedException e1) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    throw e;
                }
            }
        } while (retryTimes++ < this.maxRetryTimes);

        log.warn("重试达到最大次数【{}】", this.maxRetryTimes);
        throw new WxRuntimeException("微信服务端异常，超出重试次数");
    }

    /**
     * 获取access_token.
     */
    protected final String GET_ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";

    /**
     * The constant JSCODE_TO_SESSION_URL.
     */
    protected final String JSCODE_TO_SESSION_URL = "https://api.weixin.qq.com/sns/jscode2session";
    /**
     * getPaidUnionId
     */
    protected final String GET_PAID_UNION_ID_URL = "https://api.weixin.qq.com/wxa/getpaidunionid";
    /**
     * 导入抽样数据
     */
    protected final String SET_DYNAMIC_DATA_URL = "https://api.weixin.qq.com/wxa/setdynamicdata";

    private <T, E> T executeInternal(RequestExecutor<T, E> executor, String uri, E data, boolean doNotAutoRefreshToken)
            throws WxErrorException {
        E dataForLog = DataUtils.handleDataWithSecret(data);

        if (uri.contains("access_token=")) {
            throw new IllegalArgumentException("uri参数中不允许有access_token: " + uri);
        }
        String accessToken = getAccessToken(false);

        if (StringUtils.isNotEmpty(this.getWxMaConfig().getApiHostUrl())) {
            uri = uri.replace("https://api.weixin.qq.com", this.getWxMaConfig().getApiHostUrl());
        }

        String uriWithAccessToken = uri + (uri.contains("?") ? "&" : "?") + "access_token=" + accessToken;

        try {
            T result = executor.execute(uriWithAccessToken, data, WxType.MiniApp);
            log.debug("\n【请求地址】: {}\n【请求参数】：{}\n【响应数据】：{}", uriWithAccessToken, dataForLog, result);
            return result;
        } catch (WxErrorException e) {
            WxError error = e.getError();
            if (WxConsts.ACCESS_TOKEN_ERROR_CODES.contains(error.getErrorCode())) {
                // 强制设置WxMaConfig的access token过期了，这样在下一次请求里就会刷新access token
                Lock lock = this.getWxMaConfig().getAccessTokenLock();
                lock.lock();
                try {
                    if (StringUtils.equals(this.getWxMaConfig().getAccessToken(), accessToken)) {
                        this.getWxMaConfig().expireAccessToken();
                    }
                } catch (Exception ex) {
                    this.getWxMaConfig().expireAccessToken();
                } finally {
                    lock.unlock();
                }
                if (this.getWxMaConfig().autoRefreshToken() && !doNotAutoRefreshToken) {
                    log.warn("即将重新获取新的access_token，错误代码：{}，错误信息：{}", error.getErrorCode(), error.getErrorMsg());
                    // 下一次不再自动重试
                    // 当小程序误调用第三方平台专属接口时,第三方无法使用小程序的access token,如果可以继续自动获取token会导致无限循环重试,直到栈溢出
                    return this.executeInternal(executor, uri, data, true);
                }
            }

            if (error.getErrorCode() != 0) {
                log.error("\n【请求地址】: {}\n【请求参数】：{}\n【错误信息】：{}", uriWithAccessToken, dataForLog, error);
                throw new WxErrorException(error, e);
            }
            return null;
        } catch (IOException e) {
            log.error("\n【请求地址】: {}\n【请求参数】：{}\n【异常信息】：{}", uriWithAccessToken, dataForLog, e.getMessage());
            throw new WxRuntimeException(e);
        }
    }

    protected String getAccessToken() throws WxErrorException {
        return getAccessToken(false);
    }

    protected String getAccessToken(boolean forceRefresh) throws WxErrorException {
        if (!forceRefresh && !this.getWxMaConfig().isAccessTokenExpired()) {
            return this.getWxMaConfig().getAccessToken();
        }

        Lock lock = this.getWxMaConfig().getAccessTokenLock();
        boolean locked = false;
        try {
            do {
                locked = lock.tryLock(100, TimeUnit.MILLISECONDS);
                if (!forceRefresh && !this.getWxMaConfig().isAccessTokenExpired()) {
                    return this.getWxMaConfig().getAccessToken();
                }
            } while (!locked);
            String response = doGetAccessTokenRequest();
            return extractAccessToken(response);
        } catch (IOException | InterruptedException e) {
            throw new WxRuntimeException(e);
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
    }

    protected abstract String doGetAccessTokenRequest() throws IOException;

    /**
     * 设置当前的AccessToken
     *
     * @param resultContent 响应内容
     * @return access token
     * @throws WxErrorException 异常
     */
    protected String extractAccessToken(String resultContent) throws WxErrorException {
        log.info("resultContent: " + resultContent);
        WxConfig config = this.getWxMaConfig();
        WxError error = WxError.fromJson(resultContent, WxType.MiniApp);
        if (error.getErrorCode() != 0) {
            throw new WxErrorException(error);
        }
        WxAccessToken accessToken = WxAccessToken.fromJson(resultContent);
        config.updateAccessToken(accessToken.getAccessToken(), accessToken.getExpiresIn());
        return accessToken.getAccessToken();
    }

    public String get(String url, String queryParam) throws WxErrorException {
        return execute(SimpleGetRequestExecutor.create((RequestHttp<?, ?>) this), url, queryParam);
    }

    public String post(String url, String postData) throws WxErrorException {
        return execute(SimplePostRequestExecutor.create((RequestHttp<?, ?>) this), url, postData);
    }
}
