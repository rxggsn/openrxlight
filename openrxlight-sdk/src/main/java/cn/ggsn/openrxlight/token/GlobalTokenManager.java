package cn.ggsn.openrxlight.token;

import java.util.Map;

import com.google.common.collect.Maps;

public class GlobalTokenManager {
    private static Map<String, ICache> CACHE = Maps.newHashMap();

    public static ICache getCache(String clientId) {
        return CACHE.get(clientId);
    }

    public static void setCache(String clientId, ICache cache) {
        CACHE.put(clientId, cache);
    }
}
