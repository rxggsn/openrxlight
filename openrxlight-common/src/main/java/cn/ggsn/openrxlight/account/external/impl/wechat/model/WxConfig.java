package cn.ggsn.openrxlight.account.external.impl.wechat.model;

import java.util.concurrent.locks.Lock;

public interface WxConfig {
    String getAccessToken();

    Lock getAccessTokenLock();

    boolean isAccessTokenExpired();

    void expireAccessToken();

    void updateAccessToken(WxAccessToken token);

    void updateAccessToken(String var1, int var2);

    String getJsapiTicket();

    Lock getJsapiTicketLock();

    boolean isJsapiTicketExpired();

    void expireJsapiTicket();

    void updateJsapiTicket(String var1, int var2);

    String getCardApiTicket();

    Lock getCardApiTicketLock();

    boolean isCardApiTicketExpired();

    void expireCardApiTicket();

    void updateCardApiTicket(String var1, int var2);

    String getAppid();

    String getSecret();

    String getToken();

    String getAesKey();

    String getOriginalId();

    String getCloudEnv();

    String getMsgDataFormat();

    long getExpiresTime();

    String getHttpProxyHost();

    int getHttpProxyPort();

    String getHttpProxyUsername();

    String getHttpProxyPassword();

    int getRetrySleepMillis();

    int getMaxRetryTimes();

    boolean autoRefreshToken();

    void setApiHostUrl(String var1);

    String getApiHostUrl();

    String getAccessTokenUrl();

    void setAccessTokenUrl(String var1);
}
