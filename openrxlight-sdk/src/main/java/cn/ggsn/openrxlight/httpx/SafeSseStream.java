package cn.ggsn.openrxlight.httpx;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import cn.ggsn.openrxlight.utils.JsonUtils;
import com.google.common.collect.Lists;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;

/**
 * Safe Server-Sent Events (SSE) Stream handler.
 * This class is intended to provide a thread-safe way to consume SSE streams
 * (Multi-Consumer, Single-Producer),
 * ensuring that events are processed without concurrency issues.
 */
@Slf4j
public class SafeSseStream<T> extends EventSourceListener implements Iterator<T> {
    private static final ThreadLocal<AtomicInteger> THREAD_LOCAL_COUNTER = ThreadLocal.withInitial(AtomicInteger::new);

    @Setter
    private EventSource eventSource;
    private volatile boolean isClosed = false;
    private final Class<T> clazz;
    private volatile List<String> buffer;

    public SafeSseStream(Class<T> clazz) {
        this.clazz = clazz;
        this.buffer = Lists.newArrayList();
    }

    @Override
    public boolean hasNext() {
        return !isClosed;
    }

    @Override
    public T next() {
        int threadIndex = THREAD_LOCAL_COUNTER.get().getAndIncrement();
        try {
            String nextVal = this.buffer.get(threadIndex);
            return JsonUtils.fromJson(nextVal, this.clazz);
        } catch (IndexOutOfBoundsException e) {
            log.debug("Error while parsing SSE event: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void onClosed(EventSource eventSource) {
        eventSource.cancel();
        isClosed = true;
        synchronized (this.buffer) {
            this.buffer.clear();
        }
    }

    @Override
    public void onEvent(EventSource eventSource, String id, String type, String data) {
        log.debug("id:{},type:{},data:{}", id, type, data);
        synchronized (this.buffer) {
            this.buffer.add(data);
        }
    }

    @Override
    public void onFailure(EventSource eventSource, Throwable t, Response response) {
        log.error("SSE stream error happens: {}", t);

        throw new RuntimeException("SSE stream error", t);
    }

    @Override
    public void onOpen(EventSource eventSource, Response response) {
        super.onOpen(eventSource, response);
    }
}
