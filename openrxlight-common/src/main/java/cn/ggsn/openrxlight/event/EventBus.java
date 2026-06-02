package cn.ggsn.openrxlight.event;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.google.common.collect.Sets;

import cn.ggsn.openrxlight.lang.Lists2;
import cn.ggsn.openrxlight.lang.Maps2;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class EventBus {
    protected Map<String, SubjectConsumer> subscribedTopics;
    protected final EventBusType eventBusType;
    protected final Set<String> subscriptions;

    protected EventBus(EventBusType eventBusType) {
        this.subscribedTopics = Maps2.empty();
        this.eventBusType = eventBusType;
        this.subscriptions = Sets.newHashSet();
    }

    public abstract <T> void publish(CloudEvent<T> event);

    protected abstract <T> List<CloudEvent<T>> poll(String subject, Class<T> dataType);

    public void poll() {
        List<Future<Void>> futures = Lists2.empty();
        ExecutorService executors = Executors.newVirtualThreadPerTaskExecutor();
        this.subscribedTopics.forEach((subject, consumer) -> {
            futures.add(
                    CompletableFuture.supplyAsync(() -> {
                        return this.poll(subject, consumer.getEventType());
                    }, executors).thenAccept((events) -> {
                        Lists2.foreach(events, event -> {
                            if (Objects.nonNull(event)) {
                                consumer.post(event);
                            }
                        });
                    }));
        });

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // Lists2.foreach(futures, (future) -> {
        // try {
        // future.get();
        // } catch (Exception e) {
        // log.error("process eventss failure: {}", e);
        // }
        // });
    }

    public boolean supports(EventBusType type) {
        return this.eventBusType == type;
    }

    public void subscribe(String subject) {
        this.subscriptions.add(subject);
    }

    protected abstract void init();

    /**
     * close the EventBus nicely
     * 
     */
    public abstract void close();
}
