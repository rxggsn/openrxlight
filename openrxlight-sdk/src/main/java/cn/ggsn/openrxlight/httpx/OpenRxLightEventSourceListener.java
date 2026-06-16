package cn.ggsn.openrxlight.httpx;

import java.util.concurrent.atomic.AtomicReference;

// import java.util.concurrent.atomic.AtomicReference;

import cn.ggsn.openrxlight.utils.JsonUtils;
import io.reactivex.Flowable;
import lombok.extern.slf4j.Slf4j;
import io.reactivex.BackpressureStrategy;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;

@Slf4j
public class OpenRxLightEventSourceListener<T> extends EventSourceListener {

    private final AtomicReference<io.reactivex.FlowableEmitter<T>> emitterRef = new AtomicReference<>();
    // private final BehaviorProcessor<T> processor = BehaviorProcessor.<T>create();
    private final Class<T> clazz;

    public OpenRxLightEventSourceListener(Class<T> clazz) {
        this.clazz = clazz;
    }

    public Flowable<T> events() {
        // return Flowable.fromPublisher(this.processor);
        return Flowable.create(emitter -> {
            emitterRef.setRelease(emitter);
            emitter.setCancellable(() -> emitterRef.compareAndSet(emitter, null));
        }, BackpressureStrategy.BUFFER);
    }

    @Override
    public void onClosed(EventSource eventSource) {
        io.reactivex.FlowableEmitter<T> emitter = emitterRef.getAndSet(null);
        if (emitter != null && !emitter.isCancelled()) {
            log.info("event sorce is closed");
            emitter.onComplete();
        }
        // this.processor.onComplete();
    }

    @Override
    public void onEvent(EventSource eventSource, String id, String type, String data) {
        // this.processor.offer(JsonUtils.fromJson(data, this.clazz));
        log.debug("event sorce has next data {}", data);
        io.reactivex.FlowableEmitter<T> emitter = emitterRef.getAcquire();
        if (emitter != null && !emitter.isCancelled()) {
            emitter.onNext(JsonUtils.fromJson(data, this.clazz));
        }
    }

    @Override
    public void onFailure(EventSource eventSource, Throwable t, Response response) {
        io.reactivex.FlowableEmitter<T> emitter = emitterRef.getAndSet(null);
        log.error("error happens when listening openrxlight sse {}", response.message());
        if (emitter != null && !emitter.isCancelled() && t != null) {
            emitter.onError(t);
        }

        // if (t != null) {
        // this.processor.onError(t);
        // }
    }
}
