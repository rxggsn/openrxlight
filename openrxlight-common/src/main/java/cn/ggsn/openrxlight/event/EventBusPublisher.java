package cn.ggsn.openrxlight.event;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;

import cn.ggsn.openrxlight.lang.Lists2;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EventBusPublisher {
    private final List<EventBus> eventBuses;

    EventBusPublisher(List<EventBus> eventBuses) {
        log.info("Initialized EventBusPublisher with event buses {}", Lists2.map(eventBuses, bus -> bus.eventBusType));
        this.eventBuses = eventBuses;
    }

    public <T> void publish(CloudEvent<T> event, EventBusType type) {
        for (EventBus eventBus : eventBuses) {
            if (eventBus.supports(type)) {
                eventBus.publish(event);
                return;
            }
        }
        throw new RuntimeException("No supported EventBus found for type: " + type);
    }

    @SuppressWarnings("null")
    public void addTarget(EventBusType type, String subject, String dataschema, Class<?> dataType, Object bean,
            Method method) {
        Lists2.foreach(this.eventBuses, eventbus -> {
            if (eventbus.supports(type)) {
                SubjectConsumer consumer = eventbus.subscribedTopics.get(subject);
                if (consumer == null) {
                    consumer = new SubjectConsumer(Objects.requireNonNull(subject),
                            Objects.requireNonNull(dataType),
                            Executors.newVirtualThreadPerTaskExecutor());
                    eventbus.subscribedTopics.put(subject, consumer);
                }
                eventbus.subscribe(subject);
                method.setAccessible(true);
                Processor processor = new Processor(bean, method);
                consumer.register(dataschema, processor);
            }
        });

    }
}
