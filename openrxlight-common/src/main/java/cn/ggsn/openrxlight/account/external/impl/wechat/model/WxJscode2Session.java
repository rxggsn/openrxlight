package cn.ggsn.openrxlight.account.external.impl.wechat.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class WxJscode2Session {
    @JsonProperty("session_key")
    private String sessionKey;
    @JsonProperty("openid")
    private String openid;
    @JsonProperty("unionid")
    private String unionid;
}
