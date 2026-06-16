package cn.ggsn.rxlight.ai.agent.vo;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import cn.ggsn.rxlight.ai.domain.AppType;
import io.vertx.redis.client.RedisAPI;
import lombok.Getter;

public abstract class AgentContext {
    @Getter
    private final int agentId;
    @Getter
    private final String appId;
    @Getter
    private final AppType appType;
    public volatile boolean hasRemaingMessages = false;
    protected final Cache<String, String> rxlightMessageIdMap;
    private final Cache<String, String> userLocationMap;

    public AgentContext(int agentId, RedisAPI redis, String appId, AppType appType) {
        this.agentId = agentId;
        this.appId = appId;
        this.appType = appType;
        this.rxlightMessageIdMap = Caffeine.newBuilder()
                .maximumSize(256)
                .expireAfterWrite(Duration.ofSeconds(600L))
                .<String, String>build();
        this.userLocationMap = Caffeine.newBuilder().maximumSize(256)
                .expireAfterWrite(Duration.ofSeconds(60L)).<String, String>build();
    }

    public void putMsgIndex(String appMessageId, String messageId) {
        this.rxlightMessageIdMap.put(appMessageId, messageId);
    }

    public void refreshLocation(UUID userId, String content) {
        this.userLocationMap.put(userId.toString(), content);
    }

    public Optional<String> getLocation(UUID userId) {
        return Optional.ofNullable(this.userLocationMap.getIfPresent(userId.toString()));
    }

    public boolean isDuplicated(String appMessageId) {
        return StringUtils.isNotBlank(this.rxlightMessageIdMap.getIfPresent(appMessageId));
    }

}
