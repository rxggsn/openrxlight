package cn.ggsn.openrxlight.event.redis;

import java.time.Duration;
import java.util.List;

import cn.ggsn.openrxlight.event.CloudEvent;
import cn.ggsn.openrxlight.event.EventBus;
import cn.ggsn.openrxlight.event.EventBusType;
import cn.ggsn.openrxlight.lang.Lists2;
import io.lettuce.core.Consumer;
import io.lettuce.core.RedisBusyException;
import io.lettuce.core.RedisClient;
import io.lettuce.core.StreamMessage;
import io.lettuce.core.XAddArgs;
import io.lettuce.core.XGroupCreateArgs;
import io.lettuce.core.XReadArgs;
import io.lettuce.core.XReadArgs.StreamOffset;
import io.lettuce.core.api.StatefulRedisConnection;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RedisEventBus extends EventBus {
    private final StatefulRedisConnection<String, String> connection;
    private static final Duration TIMOUT = Duration.ofMillis(300);
    private final String groupId;
    private int batchSize = 10;

    public RedisEventBus(String redisUrl, String groupId) {
        super(EventBusType.REDIS);
        this.connection = RedisClient.create(redisUrl).connect();
        this.groupId = groupId;
    }

    @Override
    public <T> void publish(CloudEvent<T> event) {
        String channel = event.getSubject();
        var keyValues = event.toMap();
        XAddArgs args = new XAddArgs().maxlen(1024).approximateTrimming();
        this.connection.sync().xadd(channel, args, keyValues);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> List<CloudEvent<T>> poll(String subject, Class<T> dataType) {
        try {
            List<StreamMessage<String, String>> messages = this.connection.sync().xreadgroup(
                    Consumer.from(this.groupId, subject),
                    new XReadArgs().noack(false).block(TIMOUT).count(this.batchSize),
                    StreamOffset.lastConsumed(subject));
            return Lists2.map(messages, streamMsg -> {
                CloudEvent<T> event = CloudEvent.parse(streamMsg.getBody(), dataType);
                return event;
            });
        } catch (Exception ex) {
            log.error("fetch next message failed", ex);
            return null;
        }
    }

    @Override
    protected void init() {
        this.subscriptions.forEach(topic -> {
            try {
                this.connection.sync().xgroupCreate(StreamOffset.latest(topic), this.groupId,
                        new XGroupCreateArgs().mkstream(true));
            } catch (RedisBusyException ex) {
                log.info("topic has been created, ignored");
            }
        });
    }

    @Override
    public void close() {
        this.connection.close();
    }
}
