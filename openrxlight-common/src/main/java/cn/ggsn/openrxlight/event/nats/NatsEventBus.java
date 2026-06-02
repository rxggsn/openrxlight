package cn.ggsn.openrxlight.event.nats;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import cn.ggsn.openrxlight.event.CloudEvent;
import cn.ggsn.openrxlight.event.EventBus;
import cn.ggsn.openrxlight.event.EventBusType;
import cn.ggsn.openrxlight.lang.Lists2;
import cn.ggsn.openrxlight.lang.Maps2;
import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.Nats;
import io.nats.client.Options;
import io.nats.client.Subscription;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NatsEventBus extends EventBus {
    private final Connection natsConnection;
    private final Map<String, Subscription> natsSubscriptions = Maps2.empty();
    private int batchSize = 10; // Default batch size 10
    private final String groupId;

    public NatsEventBus(NatsProperties properties, String groupId) throws Exception {
        super(EventBusType.NATS);
        this.batchSize = properties.bufferSize();
        this.groupId = groupId;
        this.natsConnection = Nats.connect(Options.builder()
                .servers(properties.boostrapServers().orElse("").split(","))
                .bufferSize(properties.bufferSize())
                .connectionTimeout(properties.connectionTimeout())
                .maxReconnects(-1)
                .build());
    }

    @Override
    public <T> void publish(CloudEvent<T> event) {
        this.natsConnection.publish(event.getTopic(EventBusType.NATS), event.toJson().getBytes());
    }

    @Override
    protected <T> List<CloudEvent<T>> poll(String subject, Class<T> dataType) {
        Subscription subscription = this.natsSubscriptions.get(subject);
        List<CloudEvent<T>> results = Lists2.empty();
        if (Objects.nonNull(subscription)) {
            try {
                for (int i = 0; i < batchSize; i++) {
                    Message nextMessage = subscription.nextMessage(0);
                    if (Objects.isNull(nextMessage)) {
                        break;
                    }
                    results.add(CloudEvent.parseString(new String(nextMessage.getData()), dataType));
                    nextMessage.ack();
                }
            } catch (IllegalStateException ex) {
                // No more messages
                log.debug("no more messages for subject {}", subject);
            } catch (Exception ex) {
                log.error("fetch next message failed", ex);
            }
        }

        return results;
    }

    @Override
    protected void init() {
        this.subscriptions.forEach(topic -> {
            this.natsSubscriptions.put(topic,
                    this.natsConnection.subscribe(String.format("%s.*", topic), this.groupId));
        });
    }

    @Override
    public void close() {
        try {
            this.natsConnection.close();
        } catch (InterruptedException e) {
            log.error("close nats connection exceptionally", e);
        }
    }

}
