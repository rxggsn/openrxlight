package cn.ggsn.rxlight.ai.agent.impl.lark.vo;

import java.time.Duration;
import java.util.Optional;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import cn.ggsn.rxlight.ai.agent.vo.AgentContext;
import cn.ggsn.rxlight.ai.domain.AppType;
import io.vertx.redis.client.RedisAPI;

public class LarkContext extends AgentContext {
    private final Cache<String, String> cardIdLarkMessageMap;

    public LarkContext(RedisAPI redis, int agentId, String appId, AppType appType) {
        super(agentId, redis, appId, appType);
        this.cardIdLarkMessageMap = Caffeine.newBuilder().maximumSize(256)
                .expireAfterWrite(Duration.ofSeconds(60L))
                .build();
    }

    public String getCardIdByLarkMsgId(String openMessageId) {
        return Optional.ofNullable(this.rxlightMessageIdMap.getIfPresent(openMessageId))
                .flatMap(appMsgId -> Optional.ofNullable(this.cardIdLarkMessageMap.getIfPresent(appMsgId)))
                .orElse(null);
    }

    public void putCardIndex(String appMessageId, String cardId) {
        this.cardIdLarkMessageMap.put(appMessageId, cardId);
    }
}
