package cn.ggsn.openrxlight.account.external.impl.wechat.request;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.apache.http.impl.client.CloseableHttpClient;

import cn.ggsn.openrxlight.account.external.impl.wechat.error.WxError;
import cn.ggsn.openrxlight.account.external.impl.wechat.error.WxErrorException;
import cn.ggsn.openrxlight.account.external.impl.wechat.model.WxType;
import okhttp3.OkHttpClient;

/**
 * 简单的GET请求执行器.
 * 请求的参数是String, 返回的结果也是String
 *
 * @author Daniel Qian
 */
public abstract class SimpleGetRequestExecutor<H, P> implements RequestExecutor<String, String> {
    protected RequestHttp<H, P> requestHttp;

    public SimpleGetRequestExecutor(RequestHttp<H, P> requestHttp) {
        this.requestHttp = requestHttp;
    }

    @Override
    public void execute(String uri, String data, ResponseHandler<String> handler, WxType wxType)
            throws WxErrorException, IOException {
        handler.handle(this.execute(uri, data, wxType));
    }

    @SuppressWarnings("unchecked")
    public static RequestExecutor<String, String> create(RequestHttp<?, ?> requestHttp) {
        switch (requestHttp.getRequestType()) {
            case APACHE_HTTP:
                return new ApacheSimpleGetRequestExecutor((RequestHttp<CloseableHttpClient, HttpHost>) requestHttp);
            case OK_HTTP:
                return new OkHttpSimpleGetRequestExecutor((RequestHttp<OkHttpClient, OkHttpProxyInfo>) requestHttp);
            default:
                throw new IllegalArgumentException("非法请求参数");
        }
    }

    protected String handleResponse(WxType wxType, String responseContent) throws WxErrorException {
        WxError error = WxError.fromJson(responseContent, wxType);
        if (error.getErrorCode() != 0) {
            throw new WxErrorException(error);
        }

        return responseContent;
    }
}
