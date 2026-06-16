package cn.ggsn.rxlight.transaction.pay.fuyou.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import cn.ggsn.rxlight.transaction.pay.fuyou.Consts;
import cn.ggsn.rxlight.transaction.pay.fuyou.SignatureIgnore;
import cn.ggsn.rxlight.transaction.pay.fuyou.domain.FuyouSignatureKeyLink;
import cn.ggsn.rxlight.transaction.pay.fuyou.utils.SignatureUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;

@Data
@JsonRootName("xml")
@EqualsAndHashCode(callSuper = true)
public class FuyouSignatureBaseRequest extends FuyouSignatureKeyLink {
    @JsonIgnore
    private static final RandomNumberGenerator RANDOM_NUMBER_GENERATOR = new SecureRandomNumberGenerator();
    @JsonProperty("ins_cd")
    private String insCd;
    @JsonProperty("mchnt_cd")
    private String merchantCode;
    @JsonProperty("term_id")
    private String termId;
    @JsonProperty("random_str")
    private String randomStr;
    @SignatureIgnore
    private String sign;
    private String version = "1.0";

    public void generateSign(PrivateKey insPrivateKey) throws NoSuchAlgorithmException {
        String preSignStr = this.linkVariables();
        try {
            this.sign = URLEncoder.encode(SignatureUtils.generateSign(preSignStr, insPrivateKey), Consts.CHARSET);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void generateRandomStr() {
        this.randomStr = RANDOM_NUMBER_GENERATOR.nextBytes().toHex();
    }
}
