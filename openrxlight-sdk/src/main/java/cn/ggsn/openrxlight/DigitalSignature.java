package cn.ggsn.openrxlight;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.apache.commons.lang.StringUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Getter
@RequiredArgsConstructor
@Slf4j
public enum DigitalSignature {
    RSA_SHA256(Constants.RSA_KEY, Constants.RSA_SHA256),
    SM2_SM3(Constants.SM2_KEY, Constants.SM2_SM3),
    HMAC_SHA256(Constants.HMAC_KEY, Constants.HMAC_SHA256),
    RSA_MD5(Constants.RSA_KEY, Constants.RSA_MD5);

    private final String keyType;
    private final String signAlgorithm;

    public KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator;
            switch (this) {
                case RSA_SHA256:
                    keyPairGenerator = KeyPairGenerator.getInstance("RSA");
                    keyPairGenerator.initialize(2048);
                    break;
                case SM2_SM3:
                    keyPairGenerator = KeyPairGenerator.getInstance("EC", "BC");
                    keyPairGenerator.initialize(new ECGenParameterSpec("sm2p256v1"));
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported key type: " + this.keyType);
            }
            return keyPairGenerator.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate key pair for " + this.name(), e);
        }
    }

    public String getPublicKeyPem(java.security.KeyPair keyPair) {
        byte[] publicKeyBytes = keyPair.getPublic().getEncoded();
        String base64Key = Base64.getEncoder().encodeToString(publicKeyBytes);
        return "-----BEGIN PUBLIC KEY-----\n" +
                insertLineBreaks(base64Key) +
                "\n-----END PUBLIC KEY-----";
    }

    public String getPrivateKeyPem(java.security.KeyPair keyPair) {
        byte[] privateKeyBytes = keyPair.getPrivate().getEncoded();
        String base64Key = Base64.getEncoder().encodeToString(privateKeyBytes);
        return "-----BEGIN PRIVATE KEY-----\n" +
                insertLineBreaks(base64Key) +
                "\n-----END PRIVATE KEY-----";
    }

    private String insertLineBreaks(String base64) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < base64.length(); i += 64) {
            if (i > 0)
                sb.append("\n");
            sb.append(base64, i, Math.min(i + 64, base64.length()));
        }
        return sb.toString();
    }

    public PrivateKey getPrivateKey(String privKey) throws Exception {
        KeyFactory keyFactory = null;
        byte[] privateKeyBytes = Base64.getDecoder()
                .decode(StringUtils
                        .replace(privKey, "-----BEGIN PRIVATE KEY-----", "")
                        .replace("-----END PRIVATE KEY-----", "")
                        .replaceAll("\n", "")
                        .replaceAll("\r", ""));
        EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        switch (this) {
            case RSA_SHA256:
                keyFactory = KeyFactory.getInstance("RSA");
                break;
            case SM2_SM3:
                keyFactory = KeyFactory.getInstance("EC", "BC");
                break;
            case HMAC_SHA256:
                throw new IllegalArgumentException("HMAC does not support private key");
            default:
                throw new IllegalArgumentException("Unsupported key type: " + this.keyType);

        }
        return keyFactory.generatePrivate(keySpec);
    }

    public PublicKey getPublicKey(String pubKey) throws Exception {
        // log.info("Getting public key for key type {}: {}", this.keyType, StringUtils
        // .replace(pubKey, "-----BEGIN PUBLIC KEY-----", "")
        // .replace("-----END PUBLIC KEY-----", "")
        // .replaceAll("\n", "")
        // .replaceAll("\r", ""));
        KeyFactory keyFactory = null;
        byte[] publicKeyBytes = Base64.getDecoder().decode(StringUtils
                .replace(pubKey, "-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", ""));
        EncodedKeySpec keySpec = null;
        switch (this) {
            case RSA_SHA256:
                keySpec = new X509EncodedKeySpec(publicKeyBytes);
                keyFactory = KeyFactory.getInstance("RSA");
                break;
            case SM2_SM3:
                keySpec = new X509EncodedKeySpec(publicKeyBytes);
                keyFactory = KeyFactory.getInstance("EC", "BC");
                break;
            case HMAC_SHA256:
                throw new IllegalArgumentException("HMAC does not support public key");
            default:
                throw new IllegalArgumentException("Unsupported key type: " + this.keyType);

        }
        return keyFactory.generatePublic(keySpec);
    }

    public byte[] sign(byte[] plaintext, String key) throws Exception {
        switch (this) {
            case RSA_SHA256:
            case SM2_SM3:
                PrivateKey privateKey = this.getPrivateKey(key);
                Signature signature = Signature.getInstance(this.getSignAlgorithm());
                signature.initSign(privateKey);
                signature.update(plaintext);
                return signature.sign();
            case HMAC_SHA256:
                javax.crypto.Mac mac = javax.crypto.Mac.getInstance(this.getSignAlgorithm());
                javax.crypto.spec.SecretKeySpec secretKeySpec = new javax.crypto.spec.SecretKeySpec(key.getBytes(),
                        this.getSignAlgorithm());
                mac.init(secretKeySpec);
                return mac.doFinal(plaintext);
            default:
                throw new IllegalArgumentException("Unsupported key type: " + this.keyType);
        }

    }

    public void verify(byte[] plaintext, String pubKey, byte[] originSign) throws Exception {
        Signature signature = Signature.getInstance(this.getSignAlgorithm(), "BC");
        PublicKey publicKey = this.getPublicKey(pubKey);
        signature.initVerify(publicKey);
        signature.update(plaintext);
        if (!signature.verify(originSign)) {
            throw new SecurityException("Signature verification failed");
        }
    }

}
