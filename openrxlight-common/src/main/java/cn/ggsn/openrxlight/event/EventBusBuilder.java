package cn.ggsn.openrxlight.event;

import java.util.Optional;

import cn.ggsn.openrxlight.event.kafka.KafkaProperties;
import cn.ggsn.openrxlight.event.nats.NatsProperties;
import cn.ggsn.openrxlight.event.redis.RedisProperties;
import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "quarkus.event.bus")
public interface EventBusBuilder {

    /**
     * Whether the event bus is enabled.
     * 
     * @return
     */
    Optional<Boolean> enabled();

    /**
     * Get the group ID for the event bus.
     * 
     * @return
     */
    Optional<String> groupId();

    /**
     * Get Kafka properties if configured.
     * 
     * @return
     */
    Optional<KafkaProperties> kafka();

    /**
     * Get NATS properties if configured.
     * 
     * @return
     */
    Optional<NatsProperties> nats();

    /**
     * Get Redis properties if configured.
     * 
     * @return
     */
    Optional<RedisProperties> redis();
}
