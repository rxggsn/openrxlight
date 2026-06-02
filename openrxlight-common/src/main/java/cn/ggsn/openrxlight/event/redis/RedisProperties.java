package cn.ggsn.openrxlight.event.redis;

import java.util.Optional;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "quarkus.event.bus.redis")
public interface RedisProperties {

    Optional<Boolean> enabled();

    Optional<String> url();

    Optional<String> host();

    default int port() {
        return 6379;
    }

    Optional<String> password();

    default String getUrl() {
        return this.url().orElseGet(() -> {
            return this.password().map(pwd -> {
                return String.format("redis://:%s@%s:%d?protocol=resp3", pwd,
                        this.host().orElse("localhost"), this.port());
            }).orElseGet(() -> {
                String host = this.host().orElse("localhost");
                return String.format("redis://%s:%d", host, this.port());
            });

        });
    }
}
