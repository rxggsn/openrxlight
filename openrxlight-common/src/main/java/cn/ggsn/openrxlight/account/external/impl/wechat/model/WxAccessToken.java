package cn.ggsn.openrxlight.account.external.impl.wechat.model;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import cn.ggsn.openrxlight.utils.JsonUtils;
import lombok.Data;

@Data
@JsonNaming(com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy.class)
public class WxAccessToken {
    private String accessToken;
    private int expiresIn = -1;

    public static WxAccessToken fromJson(String json) {
        return JsonUtils.fromJson(json, WxAccessToken.class);
    }
}
