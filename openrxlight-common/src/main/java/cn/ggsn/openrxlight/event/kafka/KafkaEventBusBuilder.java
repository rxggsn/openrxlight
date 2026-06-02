package cn.ggsn.openrxlight.event.kafka;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

import lombok.Data;

@Data
public class KafkaEventBusBuilder {

    public static final String STRING_SERIALIZER = "org.apache.kafka.common.serialization.StringSerializer";
    public static final String STRING_DESERIALIZER = "org.apache.kafka.common.serialization.StringDeserializer";
    public static final String BYTE_SERIALIZER = "org.apache.kafka.common.serialization.BytesSerializer";
    private String brokerServers;

    private Properties producerProperties;
    private Properties consumerProperties;

    public KafkaEventBusBuilder(Map<String, Object> producerProperties, Map<String, Object> consumerProperties) {
        this.producerProperties = new Properties();
        this.consumerProperties = new Properties();
        producerProperties.forEach((key, val) -> {
            this.producerProperties.put(key, val);
        });
        consumerProperties.forEach((key, val) -> {
            this.consumerProperties.put(key, val);
        });

    }

    public KafkaEventBus build(Executor executor) {
        return new KafkaEventBus(this.getProducerProperties(), this.getConsumerProperties());
    }
}
