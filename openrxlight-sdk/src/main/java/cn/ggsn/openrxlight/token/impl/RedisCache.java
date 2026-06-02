package cn.ggsn.openrxlight.token.impl;

import cn.ggsn.openrxlight.token.ICache;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;

public class RedisCache implements ICache {

    private final StatefulRedisConnection<String, String> connection;
    private static final String TOKEN_KEY_PREFIX = "openrxlight:token";

    public RedisCache(String redisUrl) {
        this.connection = RedisClient.create(redisUrl).connect();
    }

    @Override
    public String getToken() {
        return connection.sync().get(TOKEN_KEY_PREFIX);
    }

    @Override
    public void setToken(String token) {
        connection.sync().setex(TOKEN_KEY_PREFIX, 3600, token);
    }
}
