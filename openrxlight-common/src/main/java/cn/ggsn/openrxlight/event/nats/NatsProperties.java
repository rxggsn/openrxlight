package cn.ggsn.openrxlight.event.nats;

import java.util.Optional;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "quarkus.event.bus.nats")
public interface NatsProperties {

    Optional<Boolean> enabled();

    Optional<String> boostrapServers();

    default int bufferSize() {
        return 1048576;
    }

    default long connectionTimeout() {
        return 60000;
    }

}
