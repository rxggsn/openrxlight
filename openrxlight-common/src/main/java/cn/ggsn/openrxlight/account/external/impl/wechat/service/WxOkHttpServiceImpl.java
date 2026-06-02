package cn.ggsn.openrxlight.account.external.impl.wechat.service;

import java.io.IOException;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;

import cn.ggsn.openrxlight.account.external.impl.wechat.model.WxConfig;
import cn.ggsn.openrxlight.account.external.impl.wechat.request.HttpType;
import cn.ggsn.openrxlight.account.external.impl.wechat.request.OkHttpProxyInfo;
import cn.ggsn.openrxlight.account.external.impl.wechat.request.OkHttpProxyInfo.ProxyType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public abstract class WxOkHttpServiceImpl extends BaseWxServiceImpl<OkHttpClient, OkHttpProxyInfo> {

    private final OkHttpClient httpClient;
    private final OkHttpProxyInfo httpProxy;

    protected WxOkHttpServiceImpl(WxConfig wxMaConfig) {
        super(wxMaConfig);
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(3000, java.util.concurrent.TimeUnit.MILLISECONDS)
                .readTimeout(3000, java.util.concurrent.TimeUnit.MILLISECONDS)
                .build();
        this.httpProxy = new OkHttpProxyInfo(ProxyType.HTTP, wxMaConfig.getHttpProxyHost(),
                wxMaConfig.getHttpProxyPort(), wxMaConfig.getHttpProxyUsername(), wxMaConfig.getHttpProxyPassword());
    }

    @Override
    protected String doGetAccessTokenRequest() throws IOException {
        String url = StringUtils.isNotEmpty(this.getWxMaConfig().getAccessTokenUrl())
                ? this.getWxMaConfig().getAccessTokenUrl()
                : (StringUtils.isNotEmpty(this.getWxMaConfig().getApiHostUrl())
                        ? "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s"
                                .replace("https://api.weixin.qq.com", this.getWxMaConfig().getApiHostUrl())
                        : "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s");
        url = String.format(url, this.getWxMaConfig().getAppid(), this.getWxMaConfig().getSecret());
        Request request = (new Request.Builder()).url(url).get().build();
        Response response = this.getRequestHttpClient().newCall(request).execute();
        Throwable exception = null;

        String body;
        try {
            body = ((ResponseBody) Objects.requireNonNull(response.body())).string();
        } catch (Throwable ex) {
            exception = ex;
            throw ex;
        } finally {
            if (response != null) {
                if (exception != null) {
                    try {
                        response.close();
                    } catch (Throwable ex) {
                        exception.addSuppressed(ex);
                    }
                } else {
                    response.close();
                }
            }

        }

        return body;
    }

    @Override
    public OkHttpClient getRequestHttpClient() {
        return this.httpClient;
    }

    @Override
    public OkHttpProxyInfo getRequestHttpProxy() {
        return this.httpProxy;
    }

    @Override
    public HttpType getRequestType() {
        return HttpType.OK_HTTP;
    }

}
