package cn.ggsn.rxlight.transaction.pay.fuyou.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import cn.ggsn.openrxlight.errorx.BizException;
import cn.ggsn.rxlight.transaction.pay.fuyou.Consts;
import cn.ggsn.rxlight.transaction.pay.fuyou.domain.FuyouSignatureKeyLink;
import cn.ggsn.rxlight.transaction.pay.fuyou.utils.SignatureUtils;
import cn.ggsn.openrxlight.transaction.pay.wechat.domain.WechatErrorCode;
import lombok.Getter;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

@Getter
@JsonRootName("xml")
public class FuyouSignatureBaseResponse extends FuyouSignatureKeyLink {
    @JsonProperty("result_code")
    private String resultCode;
    @JsonProperty("result_msg")
    private String resultMsg;
    @JsonProperty("ins_cd")
    private String insCd;
    @JsonProperty("sign")
    private String sign;
    @JsonProperty("mchnt_cd")
    private String mchntCd;
    @JsonProperty("term_id")
    private String termId;
    @JsonProperty("random_str")
    private String randomStr;

    public void verifySign(String publicKey) throws UnsupportedEncodingException, InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, SignatureException {
        String linked = this.linkVariables();
        if (!SignatureUtils.verifySign(linked, publicKey, this.sign)) {
            throw new BizException(WechatErrorCode.SIGN_ERROR);
        }
    }

    public boolean isSuccess() {
        return Consts.SUCCESS_RESP_CODE.equals(this.resultCode);
    }
}
