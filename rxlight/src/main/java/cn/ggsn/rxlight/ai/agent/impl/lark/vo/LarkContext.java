package cn.ggsn.rxlight.ai.agent.impl.lark.vo;

import java.time.Duration;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import cn.ggsn.rxlight.ai.agent.vo.AgentContext;
import cn.ggsn.rxlight.ai.domain.AppType;

public class LarkContext extends AgentContext {
    private final Cache<String, String> cardIdLarkMessageMap;

    public LarkContext(int agentId, String appId, AppType appType) {
        super(agentId, appId, appType);
        this.cardIdLarkMessageMap = Caffeine.newBuilder().maximumSize(256)
                .expireAfterWrite(Duration.ofSeconds(60L))
                .build();
    }

    public void putCardIndex(String appMessageId, String cardId) {
        this.cardIdLarkMessageMap.put(appMessageId, cardId);
    }
}
