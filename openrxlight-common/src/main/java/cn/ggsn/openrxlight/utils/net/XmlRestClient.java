package cn.ggsn.openrxlight.utils.net;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import cn.ggsn.openrxlight.errorx.BizException;
import cn.ggsn.openrxlight.errorx.CommonErrorCode;
import cn.ggsn.openrxlight.utils.JsonUtils;
import cn.ggsn.openrxlight.utils.XmlUtils;

import java.io.IOException;
import java.net.URLDecoder;

@Slf4j
public class XmlRestClient {
    @Setter
    private String charset;
    private HttpClientConnectionManager connectionManager;
    private RequestConfig requestConfig;

    public XmlRestClient() {
    }

    public static XmlRestClient createDefault() {
        return new XmlRestClient();
    }

    public XmlRestClient setConnectionManager(HttpClientConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
        return this;
    }

    public XmlRestClient setRequestConfig(RequestConfig requestConfig) {
        this.requestConfig = requestConfig;
        return this;
    }


    public <Req, Resp> Resp post(String url,
                                 Req req,
                                 HttpEntityBuilder<Req> builder,
                                 Class<Resp> respClass,
                                 boolean urlEncoded,
                                 boolean needRetry) throws IOException {
        HttpPost httppost = new HttpPost(url);
        HttpEntity entity = builder.build(req);
        httppost.setEntity(entity);
        int retryTimes = 1;
        if (needRetry) {
            retryTimes = 3;
        }
        log.info(
                "http post uri [{}], raw request [{}], request data {}",
                httppost.getURI().toString(),
                EntityUtils.toString(entity, this.charset),
                JsonUtils.toJson(req)
        );
        CloseableHttpClient httpClient;
        if (this.connectionManager != null) {
            httpClient = HttpClients.createMinimal(this.connectionManager);
        } else {
            httpClient = HttpClients.createDefault();
        }
        if (this.requestConfig != null) {
            httppost.setConfig(this.requestConfig);
        }
        try {
            for (int count = 0; count < retryTimes; count++) {
                try (CloseableHttpResponse httpResponse = httpClient.execute(httppost)) {
                    String body;
                    if (urlEncoded) {
                        body = URLDecoder.decode(
                                EntityUtils.toString(httpResponse.getEntity(), this.charset),
                                this.charset
                        );
                    } else {
                        body = EntityUtils.toString(httpResponse.getEntity(), this.charset);
                    }
                    log.info("http post uri [{}], raw response body [{}]", httppost.getURI().toString(), body);
                    if (httpResponse.getStatusLine().getStatusCode() < 300) {
                        Resp resp = XmlUtils.fromXml(body, respClass);
                        log.info("http post uri [{}], response [{}]", httppost.getURI().toString(), JsonUtils.toJson(resp));
                        return resp;
                    } else {
                        throw new BizException(CommonErrorCode.HttpRequestFailed, httpResponse.getStatusLine().getStatusCode(), body);
                    }
                } catch (IOException e) {
                    if (count == retryTimes - 1) {
                        throw e;
                    } else {
                        log.error("http post failed, retrying", e);
                    }
                }
            }
        } catch (IOException | ParseException e) {
            log.error("http post failed", e);
            throw new RuntimeException(e);
        } finally {
            httppost.releaseConnection();
        }
        return null;
    }
}
