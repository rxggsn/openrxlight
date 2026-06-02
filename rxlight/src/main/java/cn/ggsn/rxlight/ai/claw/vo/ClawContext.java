package cn.ggsn.rxlight.ai.claw.vo;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.google.common.cache.CacheBuilder;

import cn.ggsn.openrxlight.cache.RedisCacheLoader;
import io.vertx.redis.client.RedisAPI;
import lombok.Getter;

public abstract class ClawContext {
    @Getter
    private final int agentId;
    public volatile boolean hasRemaingMessages = false;

    protected final Map<String, String> rxlightMessageIdMap;
    private final RedisCacheLoader rxlightMessageIdCacheLoader;
    private final Map<String, String> userLocationMap;
    private final RedisCacheLoader userLocationCacheLoader;

    @SuppressWarnings("null")
    public ClawContext(int agentId, RedisAPI redis) {
        this.agentId = agentId;
        this.rxlightMessageIdCacheLoader = new RedisCacheLoader(redis,
                String.format("claw:%d:rxlightMessageIdMap", agentId));
        this.rxlightMessageIdMap = CacheBuilder.newBuilder().maximumSize(256)
                .expireAfterWrite(Duration.ofSeconds(60L))
                .removalListener(notification -> {
                    if (notification.getKey() != null) {
                        this.rxlightMessageIdCacheLoader.delete(notification.getKey().toString());
                    }
                })
                .build(this.rxlightMessageIdCacheLoader).asMap();
        this.userLocationCacheLoader = new RedisCacheLoader(redis,
                String.format("claw:%d:userLocationMap", agentId));
        this.userLocationMap = CacheBuilder.newBuilder().maximumSize(256)
                .expireAfterWrite(Duration.ofSeconds(600L))
                // .removalListener(notification -> {
                // if (notification.getKey() != null) {
                // this.userLocationCacheLoader.delete(notification.getKey().toString());
                // }
                // })
                .build(this.userLocationCacheLoader).asMap();
    }

    public void putMsgIndex(String appMessageId, String messageId) {
        this.rxlightMessageIdCacheLoader.put(appMessageId, messageId);
        this.rxlightMessageIdMap.put(appMessageId, messageId);
    }

    public void refreshLocation(UUID userId, String content) {
        this.userLocationCacheLoader.put(userId.toString(), content);
        this.userLocationMap.put(userId.toString(), content);
    }

    public Optional<String> getLocation(UUID userId) {
        return Optional.ofNullable(this.userLocationMap.get(userId.toString()));
    }

}
