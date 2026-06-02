package cn.ggsn.openrxlight.transaction.pay.wechat;

import java.nio.charset.StandardCharsets;

import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.service.impl.WxPayServiceApacheHttpImpl;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;

import cn.ggsn.openrxlight.transaction.pay.domain.PayClientBaseConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class WechatPayConfig extends PayClientBaseConfig {
    private String mchId;
    private String apiV3Key;
    private String notifyUrl;
    private String privateKey;
    private String merchantSerialNumber;
    private RSAAutoCertificateConfig config;
    private String counterpartyId;
    private String privateCertPath;
    private Boolean enabled;

    public WxPayService createWxPayService() {
        this.setOrCreateRsaAutoCertificateConfig();
        WxPayService wxPayService = new WxPayServiceApacheHttpImpl();
        WxPayConfig wxPayConfig = new WxPayConfig();
        wxPayConfig.setMchId(this.mchId);
        wxPayConfig.setNotifyUrl(this.notifyUrl);
        wxPayConfig.setApiV3Key(this.apiV3Key);
        byte[] privateKeyContent = this.privateKey.getBytes(StandardCharsets.UTF_8);
        wxPayConfig.setPrivateKeyContent(privateKeyContent);
        // byte[] privateCertContent =
        // this.privateCert.getBytes(StandardCharsets.UTF_8);
        // wxPayConfig.setPrivateCertContent(privateCertContent);
        wxPayConfig.setPrivateCertPath(this.privateCertPath);
        wxPayConfig.setSignType(this.config.getSignType());
        wxPayService.setConfig(wxPayConfig);
        return wxPayService;
    }

    private void setOrCreateRsaAutoCertificateConfig() {
        if (this.config == null) {
            this.config = new RSAAutoCertificateConfig.Builder()
                    .apiV3Key(this.apiV3Key)
                    .merchantId(this.mchId)
                    .privateKey(this.privateKey)
                    .merchantSerialNumber(this.merchantSerialNumber)
                    .build();
        }
    }

    //
    // private X509Certificate getCertificate() {
    // // 获取证书管理器实例
    // CertificatesManager certificatesManager = CertificatesManager.getInstance();
    //// 向证书管理器增加需要自动更新平台证书的商户信息
    // PrivateKey key = PemUtil.loadPrivateKey(this.privateKey);
    // Verifier verifier;
    // try {
    // certificatesManager.putMerchant(this.mchId, new WechatPay2Credentials(
    // this.mchId,
    // new PrivateKeySigner(this.merchantSerialNumber, key)),
    // this.apiV3Key.getBytes(StandardCharsets.UTF_8)
    // );
    // verifier = certificatesManager.getVerifier(this.mchId);
    // return verifier.getValidCertificate();
    // } catch (IOException | GeneralSecurityException | HttpCodeException |
    // NotFoundException e) {
    // throw new RuntimeException(e);
    // }
    // }

    // private CloseableHttpClient createHttpClient() {
    // // 获取证书管理器实例
    // CertificatesManager certificatesManager = CertificatesManager.getInstance();
    //// 向证书管理器增加需要自动更新平台证书的商户信息
    // PrivateKey key = PemUtil.loadPrivateKey(this.privateKey);
    // Verifier verifier;
    // try {
    // certificatesManager.putMerchant(this.mchId, new WechatPay2Credentials(
    // this.mchId,
    // new PrivateKeySigner(merchantSerialNumber, key)),
    // this.apiV3Key.getBytes(StandardCharsets.UTF_8)
    // );
    // verifier = certificatesManager.getVerifier(this.mchId);
    // } catch (IOException | GeneralSecurityException | HttpCodeException |
    // NotFoundException e) {
    // throw new RuntimeException(e);
    // }
    //// ... 若有多个商户号，可继续调用putMerchant添加商户信息
    //
    //// 从证书管理器中获取verifier
    // WechatPayHttpClientBuilder builder = WechatPayHttpClientBuilder.create()
    // .withMerchant(this.mchId, this.merchantSerialNumber, key)
    // .withValidator(new WechatPay2Validator(verifier));
    // return builder.build();
    // }
}
