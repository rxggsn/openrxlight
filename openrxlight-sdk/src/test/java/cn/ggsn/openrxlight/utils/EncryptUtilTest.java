
package cn.ggsn.openrxlight.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.util.Base64;

import cn.ggsn.openrxlight.DataCrypto;
import cn.ggsn.openrxlight.DigitalSignature;

@RunWith(JUnit4.class)
public class EncryptUtilTest {

    @BeforeClass
    public static void setup() {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    @Test
    public void testAesGcmEncrypt() throws Exception {
        // Arrange
        String plaintext = "Hello, World!";
        String encryptionKey = "0123456789abcdef0123456789abcdef";
        String iv = "123456789012";

        DataCrypto dataCrypto = DataCrypto.AES_GCM;
        // Act
        String encryptedText = EncryptUtil.encrypt(plaintext, encryptionKey, iv, dataCrypto);

        String decryptedText = EncryptUtil.decrypt(encryptedText, encryptionKey, iv, dataCrypto);

        assertEquals(plaintext, decryptedText);
    }

    @Test
    public void testSM4GcmEncrypt() throws Exception {
        // Arrange
        String plaintext = "Hello, World! ";
        String encryptionKey = "0123456789abcdef";
        String iv = "123456789012";
        DataCrypto dataCrypto = DataCrypto.SM4_GCM;
        // Act
        String result = EncryptUtil.encrypt(plaintext, encryptionKey, iv, dataCrypto);
        String decryptedText = EncryptUtil.decrypt(result, encryptionKey, iv, dataCrypto);

        // Assert
        assertEquals(plaintext, decryptedText);
    }

    @Test
    public void testRsaSha256SignAndVerify() throws Exception {
        // Generate RSA key pair
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        String privateKeyPem = "-----BEGIN PRIVATE KEY-----\n" +
                Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()) +
                "\n-----END PRIVATE KEY-----";
        String publicKeyPem = "-----BEGIN PUBLIC KEY-----\n" +
                Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()) +
                "\n-----END PUBLIC KEY-----";

        String plaintext = "Hello, World!";
        DigitalSignature digitalSignature = DigitalSignature.RSA_SHA256;

        // Sign
        String signature = EncryptUtil.sign(plaintext, privateKeyPem, digitalSignature);

        // Verify - should not throw exception
        EncryptUtil.verify(plaintext, signature, publicKeyPem, digitalSignature);
        assertTrue("RSA-SHA256 signature verification should pass", true);
    }

    @Test
    public void testSm2Sm3SignAndVerify() throws Exception {
        // Generate SM2 key pair
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC", "BC");
        keyPairGenerator.initialize(256);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        String privateKeyPem = "-----BEGIN PRIVATE KEY-----\n" +
                Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()) +
                "\n-----END PRIVATE KEY-----";
        String publicKeyPem = "-----BEGIN PUBLIC KEY-----\n" +
                Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()) +
                "\n-----END PUBLIC KEY-----";

        String plaintext = "Hello, World!";
        DigitalSignature digitalSignature = DigitalSignature.SM2_SM3;

        // Sign
        String signature = EncryptUtil.sign(plaintext, privateKeyPem, digitalSignature);

        // Verify - should not throw exception
        EncryptUtil.verify(plaintext, signature, publicKeyPem, digitalSignature);
        assertTrue("SM2-SM3 signature verification should pass", true);
    }

    @Test(expected = SecurityException.class)
    public void testVerifyWithTamperedSignature() throws Exception {
        // Generate RSA key pair
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        String privateKeyPem = "-----BEGIN PRIVATE KEY-----\n" +
                Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()) +
                "\n-----END PRIVATE KEY-----";
        String publicKeyPem = "-----BEGIN PUBLIC KEY-----\n" +
                Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()) +
                "\n-----END PUBLIC KEY-----";

        String plaintext = "Hello, World!";
        DigitalSignature digitalSignature = DigitalSignature.RSA_SHA256;

        // Sign
        String signature = EncryptUtil.sign(plaintext, privateKeyPem, digitalSignature);

        // Tamper with signature
        String tamperedSignature = signature.substring(0, signature.length() - 5) + "XXXXX";

        // Verify should throw SecurityException
        EncryptUtil.verify(plaintext, tamperedSignature, publicKeyPem, digitalSignature);
    }
}