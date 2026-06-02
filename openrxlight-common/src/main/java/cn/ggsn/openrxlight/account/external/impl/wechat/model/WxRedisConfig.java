package cn.ggsn.openrxlight.account.external.impl.wechat.model;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import cn.ggsn.openrxlight.lang.Lists2;
import io.vertx.mutiny.redis.client.RedisAPI;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class WxRedisConfig implements WxConfig {
    private static final String ACCESS_TOKEN_KEY = "wa:access_token:";
    protected volatile String appid;
    protected volatile String token;
    /**
     * 小程序原始ID
     */
    protected volatile String originalId;
    protected Lock accessTokenLock = new ReentrantLock();
    private volatile String msgDataFormat;
    private volatile String secret;
    private volatile String accessToken;
    private volatile String aesKey;
    private volatile long expiresTime;
    /**
     * 云环境ID
     */
    private volatile String cloudEnv;
    private volatile String httpProxyHost;
    private volatile int httpProxyPort;
    private volatile String httpProxyUsername;
    private volatile String httpProxyPassword;

    private volatile int retrySleepMillis = 1000;
    private volatile int maxRetryTimes = 5;

    private volatile String jsapiTicket;
    private volatile long jsapiTicketExpiresTime;
    /**
     * 微信卡券的ticket单独缓存.
     */
    private volatile String cardApiTicket;
    private volatile long cardApiTicketExpiresTime;
    protected volatile Lock jsapiTicketLock = new ReentrantLock();
    protected volatile Lock cardApiTicketLock = new ReentrantLock();
    private String apiHostUrl;
    private String accessTokenUrl;
    private final RedisAPI redis;

    @Override
    public boolean autoRefreshToken() {
        return true;
    }

    @Override
    public void expireAccessToken() {
        this.accessTokenLock.lock();
        this.accessToken = null;
        this.expiresTime = 0;
        this.accessTokenLock.unlock();
        this.redis.delAndAwait(Lists2.of(this.getRedisKey("access_token")));
    }

    @Override
    public void expireCardApiTicket() {
        this.cardApiTicketLock.lock();
        this.cardApiTicket = null;
        this.cardApiTicketExpiresTime = 0;
        this.redis.delAndAwait(Lists2.of(this.getRedisKey("card_api_ticket")));
        this.cardApiTicketLock.unlock();
    }

    @Override
    public void expireJsapiTicket() {
        this.jsapiTicketLock.lock();
        this.jsapiTicket = null;
        this.jsapiTicketExpiresTime = 0;
        this.redis.delAndAwait(Lists2.of(this.getRedisKey("jsapi_ticket")));
        this.jsapiTicketLock.unlock();
    }

    @Override
    public boolean isAccessTokenExpired() {
        return !this.redis.existsAndAwait(Lists2.of(this.getRedisKey("access_token"))).toBoolean();
    }

    @Override
    public boolean isCardApiTicketExpired() {
        return this.redis.existsAndAwait(Lists2.of(this.getRedisKey("card_api_ticket"))).toBoolean();
    }

    @Override
    public boolean isJsapiTicketExpired() {
        return this.redis.existsAndAwait(Lists2.of(this.getRedisKey("jsapi_ticket"))).toBoolean();
    }

    @Override
    public void setAccessTokenUrl(String tokenUrl) {
        this.accessTokenUrl = tokenUrl;
    }

    @Override
    public void updateAccessToken(WxAccessToken token) {
        this.accessTokenLock.lock();
        this.accessToken = token.getAccessToken();
        this.expiresTime = System.currentTimeMillis() + (token.getExpiresIn() - 200) * 1000L;
        this.redis.setexAndAwait(
                this.getRedisKey("access_token"),
                String.valueOf(token.getExpiresIn() - 200),
                token.getAccessToken());
        this.accessTokenLock.unlock();
    }

    @Override
    public void updateAccessToken(String token, int expiresIn) {
        this.accessTokenLock.lock();
        this.accessToken = token;
        this.expiresTime = System.currentTimeMillis() + (expiresIn - 200) * 1000L;
        this.redis.setexAndAwait(
                this.getRedisKey("access_token"),
                String.valueOf(expiresIn - 200),
                token);
        this.accessTokenLock.unlock();
    }

    @Override
    public void updateCardApiTicket(String ticket, int expiresIn) {
        this.cardApiTicketLock.lock();
        this.cardApiTicket = ticket;
        this.cardApiTicketExpiresTime = System.currentTimeMillis() + (expiresIn - 200) * 1000L;
        this.redis.setexAndAwait(
                this.getRedisKey("card_api_ticket"),
                String.valueOf(expiresIn - 200),
                ticket);
        this.cardApiTicketLock.unlock();
    }

    @Override
    public void updateJsapiTicket(String ticket, int expiresIn) {
        this.jsapiTicketLock.lock();
        this.jsapiTicket = ticket;
        this.jsapiTicketExpiresTime = System.currentTimeMillis() + (expiresIn - 200) * 1000L;
        this.redis.setexAndAwait(
                this.getRedisKey("jsapi_ticket"),
                String.valueOf(expiresIn - 200),
                ticket);
        this.jsapiTicketLock.unlock();
    }

    public String getRedisKey(String key) {
        return String.format("%s:%s:%s", ACCESS_TOKEN_KEY, this.appid, key);
    }

}
