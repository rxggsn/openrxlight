package cn.ggsn.rxlight.ai.memory;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import cn.ggsn.openrxlight.lang.Lists2;
import cn.ggsn.openrxlight.utils.JsonUtils;
import cn.ggsn.rxlight.ai.domain.AgentApp;
import cn.ggsn.rxlight.ai.event.impl.ChatMessage;
import io.vertx.mutiny.redis.client.RedisAPI;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MemoryCache {
    private final RedisAPI redis;
    private final AgentApp agentApp;
    private final Map<String, List<ChatMessage>> localCache = Maps.newConcurrentMap();
    private final Set<String> messageIdSet = Sets.newConcurrentHashSet();

    @SuppressWarnings("unchecked")
    public void insert(ChatMessage msg) {
        String key = this.buildKey(msg.getUserId().getOpenId());
        List<ChatMessage> list = this.localCache.get(key);
        if (list != null) {
            list.add(msg);
        } else {
            this.localCache.put(key, Lists2.of(msg));
        }

        if (Lists2.anyOf(msg.getChoices(), choice -> "stop".equals(choice.getFinishReason()))) {
            this.redis.lpush(Lists2.concat(Lists2.of(key), Lists2.map(list, JsonUtils::toJson)));
        }

        this.messageIdSet.add(msg.getId());
    }

    private String buildKey(String userId) {
        return String.format("memory:cache:%d:%s", this.agentApp.getId(), userId);
    }

    public boolean contains(String id) {
        if (!this.messageIdSet.contains(id)) {
            return this.redis.existsAndAwait(Lists2.of(id)).toInteger() > 0;
        }
        return true;
    }
}
