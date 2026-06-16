package cn.ggsn.openrxlight.event;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.kafka.clients.consumer.ConsumerConfig;

import cn.ggsn.openrxlight.event.kafka.KafkaEventBusBuilder;
import cn.ggsn.openrxlight.event.kafka.KafkaProperties;
import cn.ggsn.openrxlight.event.local.LocalEventBus;
import cn.ggsn.openrxlight.event.nats.NatsEventBus;
import cn.ggsn.openrxlight.event.nats.NatsProperties;
import cn.ggsn.openrxlight.event.redis.RedisEventBus;
import cn.ggsn.openrxlight.event.redis.RedisProperties;
import cn.ggsn.openrxlight.lang.Lists2;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EventBusBeanRegistry {

    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

    List<EventBus> eventBuses(EventBusBuilder eventBusBuilder, NatsProperties nats, KafkaProperties kafka,
            RedisProperties redis) throws Exception {
        List<EventBus> eventBuses = Lists2.empty();
        eventBuses.add(new LocalEventBus());

        String groupId = eventBusBuilder.groupId().orElse("default");
        if (kafka.enabled().orElse(Boolean.FALSE)) {
            Map<String, Object> consumerProperties = kafka.buildConsumerProperties();
            consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
            eventBuses.add(new KafkaEventBusBuilder(
                    kafka.buildProducerProperties(),
                    consumerProperties)
                    .build(this.executorService));
        }

        if (redis.enabled().orElse(Boolean.FALSE)) {
            eventBuses.add(new RedisEventBus(redis.getUrl(), groupId));
        }

        if (nats.enabled().orElse(Boolean.FALSE)) {
            eventBuses.add(new NatsEventBus(nats, groupId));
        }

        log.info("Initialized EventBuses");
        return eventBuses;
    }

    @Produces
    @ApplicationScoped
    @Startup
    EventBusPublisher eventBusPublisher(EventBusBuilder eventBusBuilder, NatsProperties nats, KafkaProperties kafka,
            RedisProperties redis)
            throws Exception {
        return new EventBusPublisher(this.eventBuses(eventBusBuilder, nats, kafka, redis));
    }
}
