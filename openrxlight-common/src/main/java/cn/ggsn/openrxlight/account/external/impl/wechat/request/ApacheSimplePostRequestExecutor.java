package cn.ggsn.openrxlight.account.external.impl.wechat.request;

import java.io.IOException;

import org.apache.http.Consts;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;

import cn.ggsn.openrxlight.account.external.impl.wechat.error.WxErrorException;
import cn.ggsn.openrxlight.account.external.impl.wechat.model.WxType;

/**
 * .
 *
 * @author ecoolper
 * @date 2017/5/4
 */
class ApacheSimplePostRequestExecutor extends SimplePostRequestExecutor<CloseableHttpClient, HttpHost> {
    public ApacheSimplePostRequestExecutor(RequestHttp<CloseableHttpClient, HttpHost> requestHttp) {
        super(requestHttp);
    }

    @Override
    public String execute(String uri, String postEntity, WxType wxType) throws WxErrorException, IOException {
        HttpPost httpPost = new HttpPost(uri);
        if (requestHttp.getRequestHttpProxy() != null) {
            RequestConfig config = RequestConfig.custom().setProxy(requestHttp.getRequestHttpProxy()).build();
            httpPost.setConfig(config);
        }

        if (postEntity != null) {
            StringEntity entity = new StringEntity(postEntity, Consts.UTF_8);
            entity.setContentType("application/json; charset=utf-8");
            httpPost.setEntity(entity);
        }

        try (CloseableHttpResponse response = requestHttp.getRequestHttpClient().execute(httpPost)) {
            String responseContent = Utf8ResponseHandler.INSTANCE.handleResponse(response);
            return this.handleResponse(wxType, responseContent);
        } finally {
            httpPost.releaseConnection();
        }
    }

}
