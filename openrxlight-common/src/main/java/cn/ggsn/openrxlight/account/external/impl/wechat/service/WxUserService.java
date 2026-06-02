package cn.ggsn.openrxlight.account.external.impl.wechat.service;

import java.util.Map;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;

import cn.ggsn.openrxlight.account.external.impl.wechat.error.WxErrorException;
import cn.ggsn.openrxlight.account.external.impl.wechat.model.WxConfig;
import cn.ggsn.openrxlight.account.external.impl.wechat.model.WxJscode2Session;
import cn.ggsn.openrxlight.account.external.impl.wechat.model.WxPhoneNoInfo;
import cn.ggsn.openrxlight.account.external.impl.wechat.utils.WxApiUrlConstants;
import cn.ggsn.openrxlight.utils.JsonUtils;

public class WxUserService extends WxOkHttpServiceImpl {

    public WxUserService(WxConfig wxMaConfig) {
        super(wxMaConfig);
    }

    public WxJscode2Session getSessionInfo(String code) throws WxErrorException {
        final WxConfig config = getWxMaConfig();
        Map<String, String> params = Maps.newHashMapWithExpectedSize(8);
        params.put("appid", config.getAppid());
        params.put("secret", config.getSecret());
        params.put("js_code", code);
        params.put("grant_type", "authorization_code");

        String result = get(this.JSCODE_TO_SESSION_URL, Joiner.on("&").withKeyValueSeparator("=").join(params));
        return JsonUtils.fromJson(result, WxJscode2Session.class);
    }

    public WxPhoneNoInfo getPhoneNoInfo(String code) throws WxErrorException {
        String responseContent = post(WxApiUrlConstants.User.GET_PHONE_NUMBER_URL,
                JsonUtils.toJson(Map.of("code", code)));
        var response = JsonUtils.toJsonNode(responseContent);
        boolean hasPhoneInfo = response.has("phone_info");
        if (hasPhoneInfo) {
            return JsonUtils.fromJson(response.get("phone_info"), WxPhoneNoInfo.class);
        } else {
            return null;
        }
    }

}
