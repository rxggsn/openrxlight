package cn.ggsn.openrxlight.httpx.impl;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import com.google.common.net.HttpHeaders;

import cn.ggsn.openrxlight.Constants;
import cn.ggsn.openrxlight.httpx.IHttpTransport;
import cn.ggsn.openrxlight.httpx.RawRequest;
import cn.ggsn.openrxlight.httpx.RawResponse;
import cn.ggsn.openrxlight.httpx.OpenRxLightEventSourceListener;
import cn.ggsn.openrxlight.utils.JsonUtils;
import io.reactivex.Flowable;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSources;

@Slf4j
public class OkHttpTransport implements IHttpTransport {

    private final OkHttpClient okHttpClient;

    public OkHttpTransport() {
        this.okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(30)) // connection timeout
                .readTimeout(Duration.ofSeconds(600)) // read timeout: maximum time to wait for data, 600s for SSE
                .writeTimeout(Duration.ofSeconds(600)) // write timeout
                .callTimeout(Duration.ofSeconds(600)) // total call timeout
                .build();
    }

    private RequestBody buildReqBody(RawRequest request) {
        if (request == null || request.getBody() == null) {
            return null;
        }

        Object body = request.getBody();
        if (body instanceof byte[]) {
            return RequestBody.create((byte[]) body, Constants.MEDIA_TYPE_JSON);
        }
        return RequestBody.create(JsonUtils.toJson(body), Constants.MEDIA_TYPE_JSON);

    }

    @Override
    public RawResponse execute(RawRequest request) throws Exception {
        // convert to okhttp request
        RequestBody body = buildReqBody(request);
        Request.Builder builder = new Request.Builder().url(request.getReqUrl())
                .method(request.getHttpMethod(), body);

        // set up headers
        if (Objects.nonNull(request.getHeaders())) {
            for (Map.Entry<String, List<String>> entry : request.getHeaders().entrySet()) {
                for (String value : entry.getValue()) {
                    builder.header(entry.getKey(), value);
                }
            }
        }

        // execute request
        Response response = okHttpClient.newCall(builder.build()).execute();

        // convert result to common result
        RawResponse rawResponse = new RawResponse();
        rawResponse.setStatusCode(response.code());
        rawResponse.setHeaders(response.headers().toMultimap());
        rawResponse.setBody(Objects.requireNonNull(response.body()).bytes());
        return rawResponse;

    }

    @Override
    public <T> Flowable<T> executeSse(RawRequest request, Class<T> clazz) throws Exception {
        // create event source
        EventSource.Factory factory = EventSources.createFactory(this.okHttpClient);
        // build request
        RequestBody req = buildReqBody(request);
        Request.Builder builder = new Request.Builder()
                .url(request.getReqUrl())
                .addHeader(HttpHeaders.CONTENT_TYPE, Constants.EVENT_STREAM)
                .addHeader(HttpHeaders.CONNECTION, "keep-alive")
                .addHeader(HttpHeaders.CACHE_CONTROL, "no-cache")
                .method(request.getHttpMethod(), req);

        // set up headers
        for (Map.Entry<String, List<String>> entry : request.getHeaders().entrySet()) {
            for (String value : entry.getValue()) {
                builder.header(entry.getKey(), value);
            }
        }
        // create SSE stream
        var sseStream = new OpenRxLightEventSourceListener<T>(clazz);
        // register sse stream to event source
        factory.newEventSource(builder.build(), sseStream);

        return sseStream.events();
    }
}