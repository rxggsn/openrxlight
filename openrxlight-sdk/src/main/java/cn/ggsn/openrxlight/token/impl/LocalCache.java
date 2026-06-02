package cn.ggsn.openrxlight.token.impl;

import cn.ggsn.openrxlight.token.ICache;

public class LocalCache implements ICache {
    private String tokenHolder = "";

    @Override
    public String getToken() {
        return tokenHolder;
    }

    @Override
    public void setToken(String token) {
        synchronized (this) {
            tokenHolder = token;
        }
    }
}
