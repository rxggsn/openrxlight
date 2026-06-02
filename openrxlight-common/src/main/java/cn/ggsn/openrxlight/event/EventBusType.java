package cn.ggsn.openrxlight.event;

public enum EventBusType {
    /**
     * Redis Stream Event Bus
     */
    REDIS,
    /**
     * Kafka Stream Event Bus
     */
    KAFKA,
    /**
     * Local In-Processor Event Bus
     */
    LOCAL,
    /**
     * NATS Stream Event Bus
     */
    NATS,
}
