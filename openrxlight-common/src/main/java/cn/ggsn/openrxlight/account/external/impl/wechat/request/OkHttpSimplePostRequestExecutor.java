package cn.ggsn.openrxlight.account.external.impl.wechat.request;

import java.io.IOException;
import java.util.Objects;

import cn.ggsn.openrxlight.account.external.impl.wechat.error.WxErrorException;
import cn.ggsn.openrxlight.account.external.impl.wechat.model.WxType;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * .
 *
 * @author ecoolper
 * @date 2017/5/4
 */
@Slf4j
class OkHttpSimplePostRequestExecutor extends SimplePostRequestExecutor<OkHttpClient, OkHttpProxyInfo> {
    public OkHttpSimplePostRequestExecutor(RequestHttp<OkHttpClient, OkHttpProxyInfo> requestHttp) {
        super(requestHttp);
    }

    @Override
    public String execute(String uri, String postEntity, WxType wxType) throws WxErrorException, IOException {
        RequestBody body = RequestBody.Companion.create(postEntity, MediaType.parse("text/plain; charset=utf-8"));
        Request request = new Request.Builder().url(uri).post(body).build();
        Response response = requestHttp.getRequestHttpClient().newCall(request).execute();
        return this.handleResponse(wxType, Objects.requireNonNull(response.body()).string());
    }

}
