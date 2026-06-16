package cn.ggsn.rxlight.transaction.pay.fuyou.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang.ArrayUtils.addAll;
import static org.apache.commons.lang.ArrayUtils.subarray;

/**
 * 用于产生RSA私钥和公钥
 *
 * @author mwilliam
 */
@Slf4j
public class RSAUtil {
    /**
     * 加密算法
     */
    public static final String KEY_ALGORITHM = "RSA";
    /**
     * 签名算法
     */
    public static final String SHA256_RSA_ALGORITHM = "SHA256withRSA";
    public static final String MD5_RSA_ALGORITHM = "MD5withRSA";
    public static final String RSA_ECB_PKCS1_PADDING = "RSA/ECB/PKCS1Padding";
    public static final int KEY_SIZE = 2048;
    // 公钥Key
    public static final String PUBLIC_KEY = "public_key";
    // 私钥Key
    public static final String PRIVATE_KEY = "private_key";

    public static Map<String, String> generateKeys() {
        return generateKeys(KEY_SIZE);
    }

    /**
     * 产生RSA公钥和私钥
     *
     * @param keySize
     * @return
     */
    public static Map<String, String> generateKeys(int keySize) {
        //为RSA算法创建一个KeyPairGenerator对象
        KeyPairGenerator kpg;
        try {
            kpg = KeyPairGenerator.getInstance(KEY_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("No such algorithm-->[" + KEY_ALGORITHM + "]");
        }
        //初始化KeyPairGenerator对象,密钥长度
        kpg.initialize(keySize);
        //生成密匙对
        KeyPair keyPair = kpg.generateKeyPair();
        //得到公钥
        Key publicKey = keyPair.getPublic();
        String publicKeyStr = Base64.encodeBase64String(publicKey.getEncoded());
        //得到私钥
        Key privateKey = keyPair.getPrivate();
        String privateKeyStr = Base64.encodeBase64String(privateKey.getEncoded());
        Map<String, String> keyPairMap = new HashMap<String, String>();
        keyPairMap.put(PUBLIC_KEY, publicKeyStr);
        keyPairMap.put(PRIVATE_KEY, privateKeyStr);
        RSAPublicKey rsp = (RSAPublicKey) keyPair.getPublic();
        BigInteger bit = rsp.getModulus();
        byte[] b = bit.toByteArray();
        byte[] deBase64Value = Base64.encodeBase64(b);
        String retValue = new String(deBase64Value);
        keyPairMap.put("model", retValue);
        return keyPairMap;
    }

    /**
     * <p>
     * 用私钥对信息生成数字签名
     * </p>
     *
     * @param data       已加密数据
     * @param privateKey 私钥(BASE64编码)
     * @return
     * @throws Exception
     */
    public static String sign(byte[] data, PrivateKey privateKey, String algorithm) throws InvalidKeySpecException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        Signature signature = Signature.getInstance(algorithm);
        signature.initSign(privateKey);
        signature.update(data);
        return Base64.encodeBase64String(signature.sign());
    }


    /** */
    /**
     * <p>
     * 校验数字签名
     * </p>
     *
     * @param data      已加密数据
     * @param publicKey 公钥(BASE64编码)
     * @param sign      数字签名
     * @return
     * @throws Exception
     */
    public static boolean verify(byte[] data, String publicKey, String sign, String algorithm) throws NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {
        byte[] keyBytes = Base64.decodeBase64(publicKey);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        PublicKey publicK = keyFactory.generatePublic(keySpec);
        Signature signature = Signature.getInstance(algorithm);
        signature.initVerify(publicK);
        signature.update(data);
        return signature.verify(Base64.decodeBase64(sign));
    }

    /**
     * 格式化java生成的key，一行长的，不适合pem中的-----BEGIN PUBLIC KEY-----，pem已经有换行了
     *
     * @param key
     */
    public static void formatKey(String key) {
        if (key == null) {
            return;
        }
        key = key.replace("\n", "");

        int count = (key.length() - 1) / 64 + 1;
        for (int i = 0; i < count; i++) {
            if (i + 1 == count) {
                //循环的最后一次
                System.out.println(key.substring(i * 64));
            } else {
                System.out.println(key.substring(i * 64, i * 64 + 64));
            }
        }
    }

    public static String getSHA256Str(String str) {
        MessageDigest messageDigest;
        String encdeStr = "";
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hash = messageDigest.digest(str.getBytes("UTF-8"));
            encdeStr = Hex.encodeHexString(hash);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            log.error("", e);
        }
        return encdeStr;
    }

    /**
     * 从pem格式(-----BEGIN PUBLIC KEY-----)的key获取一行key
     *
     * @param pem
     * @return
     */
    public static String pemToKey(String pem) {
        if (pem == null) {
            return "";
        }
        if (pem.indexOf("KEY-----") > 0) {
            pem = pem.substring(pem.indexOf("KEY-----") + "KEY-----".length());
        }
        if (pem.indexOf("-----END") > 0) {
            pem = pem.substring(0, pem.indexOf("-----END"));
        }
        return pem.replace("\n", "");
    }

    public static void main(String[] args) {
        Map<String, String> map = RSAUtil.generateKeys(2048);
        System.out.println(map.get(PRIVATE_KEY));
        System.out.println();
        System.out.println(map.get(PUBLIC_KEY));
//		RSAUtil.formatKey("MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBALYKTtyXKHd5WnBd\n" +
//			"ZOWF9d9f+t+k5bMFHyY4bfddZZFoo3Dnq7zkdk0XqakpG5YOOgt9pIDPA62gX47e\n" +
//			"Mr4ncGA80KoPBn6b9/7+wY96eOfuudqfvzufl4JQFwim+hvDqsBqBuA6CjRFh/Gv\n" +
//			"LGFXJiLx8zmbxedAfcAwyNGADIjxAgMBAAECgYA4hGzFidyTc0bD2gsoQ3X5mvft\n" +
//			"lWmHMhDgseZaRVHyWjVcKWElbRzZhH41OcEJznLw/Folb6ApuL/SQGQqq130zP0V\n" +
//			"RSJbPseUlDzG0HU2c+wq4ketJZU7z881rc31QMqTWVY3kVgpXN2U18EQYp6719mN\n" +
//			"1IpkxonxhKX9Er8DPQJBANsqvUT4vY6MVDu6dodM0uIfGZBxNrwe/8pPA4jmybrn\n" +
//			"UWlbTGU2OTADbnf4OPRHUAvnb1wKs0bw5pjBEwCFvtsCQQDUokKrVXpHUuOe8FyW\n" +
//			"ETV1QFgsTG2tp2wNFbF8mATcdyLo2I7q3K2uWSwYfjMIyKyXsdwqAy3SvTnWI8eZ\n" +
//			"GqMjAkEAm9JYPOhoxSeqX8jjqrCJIrGf2F1V4AxeKnVg+v2zIYqDDFgYCcGyiRt3\n" +
//			"eB5oR+1H0R7bwuHsspxmJubm3rE4jwJAIkwl6tL3zUdedcWZeY8/CzGcx1BwpIEL\n" +
//			"2bR1E37F0fXZiKtdqh58WVIC3dSFUNZlmHZU5+XpX0Osb5EH4fc89wJBAJ3IjckJ\n" +
//			"LtiUTpdTI2h/c+SHuejYwkdojjzFBPZXeFVlwQstky7iU009dk8Smtvy7FrhrcIJ\n" +
//			"0BiwjzeQf5JiX9s=\n");
    }

    public static byte[] decrypt(byte[] srcData, Key key, String algoName) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException {
        Cipher cipher = Cipher.getInstance(algoName);
        cipher.init(Cipher.DECRYPT_MODE, key);
        return doCipher(srcData, cipher, Cipher.DECRYPT_MODE);
    }

    public static byte[] encrypt(byte[] data, Key key, String algorithm) {
        try {
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return doCipher(data, cipher, Cipher.ENCRYPT_MODE);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static byte[] doCipher(byte[] data, Cipher cipher, int opMode) throws IllegalBlockSizeException, BadPaddingException {
        int blockSize;
        if (Cipher.ENCRYPT_MODE == opMode) {
            blockSize = cipher.getOutputSize(data.length) - 11;
        } else {
            blockSize = cipher.getOutputSize(data.length);
        }
        byte[] encryptedData = null;

        for (int i = 0; i < data.length; i += blockSize) {
            byte[] doFinal = cipher.doFinal(subarray(data, i, i + blockSize));
            encryptedData = addAll(encryptedData, doFinal);
        }

        return encryptedData;
    }
}
