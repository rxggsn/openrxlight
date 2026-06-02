package cn.ggsn.openrxlight.request;

import org.apache.commons.lang.StringUtils;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import cn.ggsn.openrxlight.config.Config;
import cn.ggsn.openrxlight.httpx.HttpMethod;
import cn.ggsn.openrxlight.utils.EncryptUtil;
import cn.ggsn.openrxlight.utils.JsonUtils;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@NoArgsConstructor
public class OpenRxLightRequest {
    private String data;
    private String nonce;
    private String signature;
    private Long timestamp;
    private String clientId;

    public <T> OpenRxLightRequest(T data, Config config, String path, String method) throws Exception {
        this.clientId = config.getClientId();
        this.timestamp = System.currentTimeMillis();
        this.nonce = EncryptUtil.generateNonce(config.getDataCrypto().ivLen());
        this.init(data, config, path, method);
    }

    private <T> OpenRxLightRequest init(T data, Config config, String path, String method) throws Exception {
        String jsondata = JsonUtils.toJson(data);
        if (HttpMethod.GET.getName().equals(method) || HttpMethod.DELETE.getName().equals(method)) {
            jsondata = null;
        }

        this.data = EncryptUtil.encrypt(jsondata,
                config.getClientSecret(), this.nonce,
                config.getDataCrypto());

        this.signature = EncryptUtil.sign(generatePlaintext(path, method),
                config.getSignaturePriKey(), config.getDigitalSign());
        return this;
    }

    public String generatePlaintext(String path, String method) {
        return StringUtils.join(Lists.newArrayList(method, path, Long.toString(this.timestamp), this.nonce,
                StringUtils.isBlank(this.data) ? "" : this.data), "");
    }
}
