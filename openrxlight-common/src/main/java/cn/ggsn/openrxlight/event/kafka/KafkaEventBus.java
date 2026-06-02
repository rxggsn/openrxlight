package cn.ggsn.openrxlight.event.kafka;

import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Future;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import com.google.common.collect.Lists;

import cn.ggsn.openrxlight.event.CloudEvent;
import cn.ggsn.openrxlight.event.EventBus;
import cn.ggsn.openrxlight.event.EventBusType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KafkaEventBus extends EventBus {

    private final KafkaProducer<String, String> kafkaProducer;
    private final KafkaConsumer<String, String> kafkaConsumer;
    private static final Duration TIMEOUT = Duration.ofMillis(300);

    public KafkaEventBus(Properties producerProperties, Properties consumerProperties) {
        super(EventBusType.KAFKA);
        this.kafkaProducer = new KafkaProducer<>(producerProperties);
        this.kafkaConsumer = new KafkaConsumer<>(consumerProperties);

    }

    @Override
    public <T> void publish(CloudEvent<T> event) {
        Future<RecordMetadata> future = this.kafkaProducer
                .send(new ProducerRecord<>(event.getSubject(), event.getId(), event.toJson()));
        try {
            future.get();
        } catch (Exception e) {
            log.warn("Error publishing event to Kafka topic: {}", event.getSubject(), e);
        }
    }

    @Override
    public <T> List<CloudEvent<T>> poll(String subject, Class<T> dataType) {
        List<CloudEvent<T>> events = Lists.newArrayList();
        try {
            this.kafkaConsumer.poll(TIMEOUT).records(subject).forEach(record -> {
                CloudEvent<T> event = CloudEvent.parseString(record.value(), dataType);
                events.add(event);
            });
            this.kafkaConsumer.commitSync();
        } catch (Exception e) {
            log.warn("Error polling events from Kafka topic: {}", subject, e);
        }

        return events;
    }

    @Override
    protected void init() {
        this.kafkaConsumer.subscribe(this.subscriptions);
    }

    @Override
    public void close() {
        this.kafkaConsumer.close();
        this.kafkaProducer.close();
    }
}
