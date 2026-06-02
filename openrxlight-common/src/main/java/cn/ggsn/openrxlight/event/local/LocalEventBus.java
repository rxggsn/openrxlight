package cn.ggsn.openrxlight.event.local;

import java.util.List;

import cn.ggsn.openrxlight.event.CloudEvent;
import cn.ggsn.openrxlight.event.EventBus;
import cn.ggsn.openrxlight.event.EventBusType;
import cn.ggsn.openrxlight.event.SubjectConsumer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LocalEventBus extends EventBus {

    public LocalEventBus() {
        super(EventBusType.LOCAL);
    }

    @Override
    public <T> void publish(CloudEvent<T> event) {
        SubjectConsumer subjectConsumer = this.subscribedTopics.get(event.getSubject());

        if (subjectConsumer != null) {
            subjectConsumer.post(event);
        } else {
            log.warn("No queues found for subject: {}, skip", event.getSubject());
        }
    }

    @Override
    protected <T> List<CloudEvent<T>> poll(String subject, Class<T> dataType) {
        return null;
    }

    @Override
    protected void init() {
    }

    @Override
    public void close() {
        this.subscribedTopics.clear();
    }
}
