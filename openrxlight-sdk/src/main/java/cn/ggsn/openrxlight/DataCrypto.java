package cn.ggsn.openrxlight;

import java.nio.charset.StandardCharsets;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.spec.GCMParameterSpec;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DataCrypto {
    AES_GCM("AES", Constants.GCM, Constants.NO_PADDING),
    SM4_GCM("SM4", Constants.GCM, Constants.NO_PADDING);

    private final String algorithm;
    private final String cipherMode;
    private final String padding;

    public javax.crypto.SecretKey getSecretKey(byte[] key) {
        try {
            return new javax.crypto.spec.SecretKeySpec(key, this.algorithm);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid key bytes for algorithm: " + this.algorithm, e);
        }
    }

    public javax.crypto.SecretKey generateNewSecretKey() {
        try {
            javax.crypto.KeyGenerator keyGen = javax.crypto.KeyGenerator.getInstance(this.algorithm,
                    this.getProvider());
            keyGen.init(256); // Use 256-bit key for AES and SM4
            return keyGen.generateKey();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate secret key for algorithm: " + this.algorithm, e);
        }
    }

    public int ivLen() {
        switch (this) {
            case AES_GCM:
            case SM4_GCM:
                return 12;
            default:
                return 16;
        }
    }

    public AlgorithmParameterSpec getAlgorithmSpec(String iv) {
        switch (this) {
            case AES_GCM:
            case SM4_GCM:
                return new GCMParameterSpec(128, iv.getBytes(StandardCharsets.UTF_8));
            default:
                throw new IllegalArgumentException("Unsupported algorithm for IV spec: " + this.cipherMode);
        }
    }

    public String getProvider() {
        switch (this) {
            case SM4_GCM:
                return "BC";
            case AES_GCM:
                return "SunJCE";
            default:
                return "";
        }
    }
}
