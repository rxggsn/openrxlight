package cn.ggsn.openrxlight.event;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.google.common.collect.Lists;

import cn.ggsn.openrxlight.config.Config;
import cn.ggsn.openrxlight.utils.EncryptUtil;
import cn.ggsn.openrxlight.utils.JsonUtils;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonNaming(com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy.class)
@NoArgsConstructor
public class WebhookEvent {
    public static interface Type {
        public static final int ORDER_STATUS_CHANGED = 1;
        public static final int ORDER_SETTLED = 2;
        public static final int DISPATCH_RESULT = 9;
        public static final int DI_NOTIFY = 10;
    }

    private Integer eventType;
    private String data;
    private String nonce;
    private String signature;
    private Long timestamp;

    public WebhookEvent(Integer eventType) {
        this.eventType = eventType;
        this.timestamp = System.currentTimeMillis();
    }

    public <T> T getBody(Class<T> clazz, Config config) throws Exception {
        return JsonUtils.fromJson(
                EncryptUtil.decrypt(this.data, config.getClientSecret(), this.nonce, config.getDataCrypto()),
                clazz);
    }

    public <T> void setBody(T body, Config config, String path) throws Exception {
        String plaintext = JsonUtils.toJson(body);
        this.nonce = EncryptUtil.generateNonce(config.getDataCrypto().ivLen());
        this.data = EncryptUtil.encrypt(plaintext, config.getClientSecret(), this.nonce, config.getDataCrypto());
        String signPlaintext = StringUtils.join(
                Lists.newArrayList("POST", path, Long.toString(this.timestamp), this.nonce,
                        this.data),
                "");
        this.signature = EncryptUtil.sign(signPlaintext, config.getSignaturePriKey(), config.getDigitalSign());
    }

    public void checkSignature(Config config, String path) throws Exception {
        String plaintext = StringUtils.join(
                Lists.newArrayList("POST", path, Long.toString(this.timestamp), this.nonce,
                        StringUtils.isBlank(this.data) ? "" : this.data),
                "");
        EncryptUtil.verify(plaintext, this.signature, config.getSignaturePubKey(), config.getDigitalSign());
    }

}
