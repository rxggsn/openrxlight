package cn.ggsn.openrxlight.cache;

import javax.annotation.Nonnull;

import com.google.common.cache.CacheLoader;

import cn.ggsn.openrxlight.lang.Lists2;
import io.vertx.redis.client.RedisAPI;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RedisCacheLoader extends CacheLoader<String, String> {

    private final RedisAPI redis;
    private final String bucket;

    @Override
    public String load(@Nonnull String key) throws Exception {
        return this.redis.hget(bucket, key).map(resp -> resp.toString())
                .toCompletionStage()
                .toCompletableFuture()
                .join();
    }

    public void put(String key, String value) {
        this.redis.hset(Lists2.of(bucket, key, value))
                .toCompletionStage()
                .toCompletableFuture()
                .join();
    }

    public void delete(String key) {
        this.redis.hdel(Lists2.of(bucket, key))
                .toCompletionStage()
                .toCompletableFuture()
                .join();
    }
}
