package cn.ggsn.rxlight.transaction.pay.fuyou.utils;

import org.apache.commons.lang3.StringUtils;

import cn.ggsn.rxlight.transaction.pay.fuyou.Consts;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

public class SignatureUtils {
    public static String generateSign(String preSignStr, PrivateKey insPrivateKey) throws UnsupportedEncodingException, InvalidKeySpecException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        return StringUtils.trim(
                RSAUtil.sign(preSignStr.getBytes(Consts.CHARSET), insPrivateKey, RSAUtil.MD5_RSA_ALGORITHM)
        );
    }

    public static boolean verifySign(String preSignStr, String sign, String insPublicKey) throws UnsupportedEncodingException, InvalidKeySpecException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        return RSAUtil.verify(preSignStr.getBytes(Consts.CHARSET), sign, insPublicKey, RSAUtil.MD5_RSA_ALGORITHM);
    }
}
