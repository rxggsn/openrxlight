package cn.ggsn.openrxlight.transaction.pay.wechat.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;

import cn.ggsn.openrxlight.utils.JsonUtils;
import cn.ggsn.openrxlight.transaction.pay.wechat.WechatPayConfig;
import cn.ggsn.openrxlight.transaction.pay.wechat.webhook.Headers;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class WechatNotification {
    private String id;
    @JsonProperty("create_time")
    @SerializedName("create_time")
    private String createTime;
    @JsonProperty("resource_type")
    @SerializedName("resource_type")
    private String resourceType;
    @JsonProperty("event_type")
    @SerializedName("event_type")
    private String eventType;
    private WechatNotificationResource resource;
    private String summary;

    public static <T> T decrypt(String body, WechatPayConfig payConfig, Headers headers, Class<T> tClass) {
        NotificationParser parser = new NotificationParser(payConfig.getConfig());
        T parsed = parser.parse(new RequestParam.Builder()
                .serialNumber(headers.getSerialNo())
                .signType(headers.getSignType())
                .timestamp(headers.getTimestamp())
                .nonce(headers.getNonce())
                .signature(headers.getSignature())
                .body(body)
                .build(),
                tClass);
        log.info("decrypted notification: {}", parsed);
        return parsed;
    }

    @Override
    public String toString() {
        return JsonUtils.toJson(this);
    }

    @Data
    public static class WechatNotificationResource {
        private String algorithm;
        private String ciphertext;
        private String nonce;
        @JsonProperty("associated_data")
        @SerializedName("associated_data")
        private String associatedData;
        @JsonProperty("original_type")
        @SerializedName("original_type")
        private String originalType;
    }
}
