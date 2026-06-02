package cn.ggsn.openrxlight.response;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import cn.ggsn.openrxlight.config.Config;
import cn.ggsn.openrxlight.utils.EncryptUtil;
import cn.ggsn.openrxlight.utils.JsonUtils;
import com.google.common.collect.Lists;

import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class OpenRxLightResponse {
    private String data;
    private String nonce;
    private String signature;
    private Long timestamp;

    public <T> T getBody(Class<T> clazz, Config config) throws Exception {
        return JsonUtils.fromJson(
                EncryptUtil.decrypt(this.data, config.getClientSecret(), this.nonce, config.getDataCrypto()),
                clazz);
    }

    public void checkSignature(Config config, String method, String path) throws Exception {
        String plaintext = StringUtils.join(
                Lists.newArrayList(method, path, Long.toString(this.timestamp), this.nonce,
                        StringUtils.isBlank(this.data) ? "" : this.data),
                "");
        EncryptUtil.verify(plaintext, this.signature, config.getSignaturePubKey(), config.getDigitalSign());
    }

    public <T> PageResult<T> getPageResult(Class<T> clazz, Config config) throws Exception {
        String decrypt = EncryptUtil.decrypt(this.data, config.getClientSecret(), this.nonce, config.getDataCrypto());
        JsonNode node = JsonUtils.toJsonNode(decrypt);
        PageResult<T> pageResult = new PageResult<>();
        if (node.get("total_count") != null && !node.get("total_count").isNull()) {
            pageResult.setTotalCount(node.get("total_count").asLong());
        }
        if (node.get("total_page") != null && !node.get("total_page").isNull()) {
            pageResult.setTotalPage(node.get("total_page").asLong());
        }
        if (node.get("page_no") != null && !node.get("page_no").isNull()) {
            pageResult.setPageNo(node.get("page_no").asInt());
        }
        if (node.get("page_size") != null && !node.get("page_size").isNull()) {
            pageResult.setPageSize(node.get("page_size").asInt());
        }
        JsonNode results = node.get("results");
        if (results != null && !results.isNull()) {
            pageResult.setResults(JsonUtils.fromArrayJson(results, clazz));
        }
        return pageResult;
    }

    public static <T> OpenRxLightResponse create(Config config, String path, String method, T entity) {
        try {
            String data = JsonUtils.toJson(entity);
            String nonce = EncryptUtil.generateNonce(config.getDataCrypto().ivLen());
            String ciphertext = EncryptUtil.encrypt(data, config.getClientSecret(), nonce, config.getDataCrypto());
            long timestamp = System.currentTimeMillis();

            String actualPath = StringUtils
                    .join(new String[] { config.getBaseUrl() != null ? config.getBaseUrl() : "", path }, "");
            String plaintext = StringUtils.join(
                    Lists.newArrayList(method, actualPath, Long.toString(timestamp), nonce, ciphertext),
                    "");
            String signature = EncryptUtil.sign(plaintext, config.getSignaturePriKey(), config.getDigitalSign());
            OpenRxLightResponse response = new OpenRxLightResponse();
            response.setTimestamp(timestamp);
            response.setData(ciphertext);
            response.setNonce(nonce);
            response.setSignature(signature);
            return response;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
