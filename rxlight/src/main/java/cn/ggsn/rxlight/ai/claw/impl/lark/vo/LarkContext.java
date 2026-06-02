package cn.ggsn.rxlight.ai.claw.impl.lark.vo;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import com.google.common.cache.CacheBuilder;
import cn.ggsn.openrxlight.cache.RedisCacheLoader;
import cn.ggsn.rxlight.ai.claw.vo.ClawContext;
import io.vertx.redis.client.RedisAPI;

public class LarkContext extends ClawContext {
    private final Map<String, String> cardIdLarkMessageMap;
    private final RedisCacheLoader cardIdCacheLoader;

    @SuppressWarnings("null")
    public LarkContext(RedisAPI redis, int agentId) {
        super(agentId, redis);
        this.cardIdCacheLoader = new RedisCacheLoader(redis, "claw:lark:cardIdMessageMap");
        this.cardIdLarkMessageMap = CacheBuilder.newBuilder().maximumSize(256)
                .expireAfterWrite(Duration.ofSeconds(60L))
                .removalListener(notification -> {
                    if (notification.getKey() != null) {
                        this.cardIdCacheLoader.delete(notification.getKey().toString());
                    }
                })
                .build(this.cardIdCacheLoader).asMap();
    }

    public String getCardIdByLarkMsgId(String openMessageId) {
        return Optional.ofNullable(this.rxlightMessageIdMap.get(openMessageId))
                .flatMap(appMsgId -> Optional.ofNullable(this.cardIdLarkMessageMap.get(appMsgId))).orElse(null);
    }

    public void putCardIndex(String appMessageId, String cardId) {
        this.cardIdCacheLoader.put(appMessageId, cardId);
        this.cardIdLarkMessageMap.put(appMessageId, cardId);
    }
}
