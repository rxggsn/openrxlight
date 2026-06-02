package cn.ggsn.openrxlight.web.config;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import cn.ggsn.openrxlight.web.InMemoryTokenStore;
import cn.ggsn.openrxlight.web.RedisTokenStore;
import cn.ggsn.openrxlight.web.UniTokenStore;
import cn.ggsn.openrxlight.web.BlockingTokenStore;
import io.vertx.mutiny.redis.client.RedisAPI;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Produces;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class TokenStoreBuilder {

    @ConfigProperty(name = "openrxlight.token.store.type", defaultValue = "IN_MEMORY")
    TokenStoreType type;

    @Produces
    BlockingTokenStore blockingTokenStore(Instance<RedisAPI> redisAPI) {
        if (type == TokenStoreType.REDIS && redisAPI.isResolvable()) {
            log.info("Using RedisTokenStore");
            return new RedisTokenStore(redisAPI.get());
        }

        log.info("Using InMemoryTokenStore");
        return new InMemoryTokenStore();
    }

    @Produces
    UniTokenStore uniTokenStore(Instance<RedisAPI> redisAPI) {
        if (type == TokenStoreType.REDIS && redisAPI.isResolvable()) {
            log.info("Using RedisTokenStore for UniTokenStore");
            return new RedisTokenStore(redisAPI.get());
        }

        log.info("Using InMemoryTokenStore for UniTokenStore");
        return new InMemoryTokenStore();
    }

}
