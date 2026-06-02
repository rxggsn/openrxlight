package cn.ggsn.openrxlight.account.external.impl.wechat;

import cn.ggsn.openrxlight.account.external.impl.wechat.error.WxErrorException;
import cn.ggsn.openrxlight.account.external.impl.wechat.model.WxJscode2Session;
import cn.ggsn.openrxlight.account.external.impl.wechat.model.WxPhoneNoInfo;
import cn.ggsn.openrxlight.account.external.impl.wechat.service.WxService;
import cn.ggsn.openrxlight.errorx.BizException;
import lombok.AccessLevel;
import lombok.Getter;

public class WechatMiniProgramApi {
    private final WxService wxMaService;
    @Getter(AccessLevel.MODULE)
    private final String appId;

    public WechatMiniProgramApi(WxService wxMaService, String appId) {
        this.wxMaService = wxMaService;
        this.appId = appId;
    }

    public WxJscode2Session getSessionInfo(String code) {
        try {
            return this.wxMaService.getUserService().getSessionInfo(code);
        } catch (WxErrorException e) {
            throw new BizException(e.getError().getErrorCode(), e.getError().getErrorMsg());
        }
    }

    public WxPhoneNoInfo getPhoneNumber(String code) {
        try {
            return this.wxMaService.getUserService().getPhoneNoInfo(code);
        } catch (WxErrorException e) {
            throw new BizException(e.getError().getErrorCode(), e.getError().getErrorMsg());
        }
    }
}
