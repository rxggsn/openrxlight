package cn.ggsn.openrxlight.httpx;

import java.util.Iterator;
import java.util.Queue;
import org.apache.commons.lang.StringUtils;

import cn.ggsn.openrxlight.errorx.ErrorResponse;
import cn.ggsn.openrxlight.utils.JsonUtils;
import com.google.common.collect.Queues;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;

/*
UnsafeSseStream is an implementation of an SSE (Server-Sent Events) stream that allows
consumers to iterate over incoming events of type T. It extends EventSourceListener to
handle SSE events and implements Iterator<T> to provide a way to access the events.

This class is marked as "unsafe" because it does not implement any synchronization
mechanisms to handle concurrent access to the internal buffer of events. As a result,
it is not thread-safe and should be used with caution in multi-threaded environments.
 */
@Slf4j
public class UnsafeSseStream<T> extends EventSourceListener implements Iterator<T> {
    @Setter
    private EventSource eventSource;
    private volatile boolean isClosed = false;
    private volatile boolean hasNext = false;
    private final Queue<String> buffer;
    private final Class<T> clazz;
    @Getter
    private Exception failureException;

    public UnsafeSseStream(Class<T> clazz) {
        this.clazz = clazz;
        this.buffer = Queues.newArrayDeque();
    }

    @Override
    public boolean hasNext() {
        return !isClosed && hasNext;
    }

    @Override
    public T next() {
        if (this.failureException != null) {
            if (this.failureException instanceof ErrorResponse) {
                throw (ErrorResponse) this.failureException;
            } else if (this.failureException instanceof RuntimeException) {
                throw (RuntimeException) this.failureException;
            } else {
                throw new RuntimeException(this.failureException);
            }
        }
        String nextVal = this.buffer.poll();
        if (StringUtils.isBlank(nextVal)) {
            hasNext = false;
            return null;
        }
        hasNext = true;
        return JsonUtils.fromJson(nextVal, this.clazz);
    }

    @Override
    public void onClosed(EventSource eventSource) {
        this.close();
    }

    @Override
    public void onEvent(EventSource eventSource, String id, String type, String data) {
        this.buffer.add(data);
        hasNext = true;
    }

    @Override
    public void onFailure(EventSource eventSource, Throwable t, Response response) {
        log.error("SSE stream error", t);
        isClosed = true;
        hasNext = false;
        if (!response.isSuccessful()) {
            var errResp = JsonUtils.fromJson(response.body().byteStream(), ErrorResponse.class);
            this.failureException = errResp;
        }
    }

    @Override
    public void onOpen(EventSource eventSource, Response response) {
        super.onOpen(eventSource, response);
    }

    public void close() {
        if (eventSource != null) {
            eventSource.cancel();
        }
        isClosed = true;
        hasNext = false;
    }

    public boolean isClosed() {
        return isClosed;
    }
}
