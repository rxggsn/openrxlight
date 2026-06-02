package cn.ggsn.openrxlight.event.kafka;

import java.util.Map;
import java.util.Optional;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "quarkus.event.bus.kafka")
public interface KafkaProperties {

    Optional<Boolean> enabled();

    default String[] bootstrapServers() {
        return new String[] { "localhost:9092" };
    }

    Optional<String> clientId();

    default String acks() {
        return "all";
    }

    default Integer retries() {
        return 3;
    }

    default Integer batchSize() {
        return 2048;
    }

    Optional<Integer> lingerMs();

    default Integer bufferMemory() {
        return 33554432;
    }

    default Map<String, Object> buildConsumerProperties() {
        return Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, this.bootstrapServers(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest",
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
    }

    default Map<String, Object> buildProducerProperties() {
        Map<String, Object> produceConfig = Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.bootstrapServers(),
                ProducerConfig.CLIENT_ID_CONFIG, this.clientId(),
                ProducerConfig.ACKS_CONFIG, this.acks(),
                ProducerConfig.RETRIES_CONFIG, this.retries(),
                ProducerConfig.BATCH_SIZE_CONFIG, this.batchSize(),
                ProducerConfig.BUFFER_MEMORY_CONFIG, this.bufferMemory());
        this.lingerMs().ifPresent(lingerMs -> {
            produceConfig.put(ProducerConfig.LINGER_MS_CONFIG, lingerMs);
        });
        return produceConfig;
    }
}
