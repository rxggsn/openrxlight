package cn.ggsn.openrxlight.translator;

import com.aliyun.alimt20181012.models.GetDetectLanguageRequest;
import com.aliyun.alimt20181012.models.GetDetectLanguageResponse;

import io.quarkus.arc.properties.IfBuildProperty;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@IfBuildProperty(name = "openrxlight.ai.translator.type", stringValue = "alicloud")
class AliTranslator implements Translator {
    private final com.aliyun.alimt20181012.Client client;

    AliTranslator(AliTranslatorConfig config) throws Exception {
        com.aliyun.teaopenapi.models.Config aliconfig = new com.aliyun.teaopenapi.models.Config()
                .setRegionId(config.regionId())
                .setAccessKeyId(config.accessKey())
                .setAccessKeySecret(config.secretKey())
                .setEndpoint(config.endpoint());
        this.client = new com.aliyun.alimt20181012.Client(aliconfig);
    }

    @Override
    public String getI18n(String text) {
        GetDetectLanguageRequest detectLanguageRequest = new GetDetectLanguageRequest()
                .setSourceText(text);

        try {
            GetDetectLanguageResponse response = this.client.getDetectLanguage(detectLanguageRequest);
            return response.getBody().getDetectedLanguage();
        } catch (Exception e) {
            log.error("Error detecting language for text: {}", text, e);
            return "unrecognized"; // Return UNRECOGNIZED if an error occurs
        }
    }
}
