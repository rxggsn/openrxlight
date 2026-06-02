package cn.ggsn.openrxlight.audio.alibaba;

import io.quarkus.arc.properties.IfBuildProperty;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;

@Dependent
public class AlicloudAudioRecognizerFactory {

    @Produces
    @IfBuildProperty(name = "openrxlight.ai.audio.type", stringValue = "alicloud")
    public AlicloudAudioRecognizerImpl alicloudAudioRecognizer(AlicloudAudioRecognitionConfig config) {
        return new AlicloudAudioRecognizerImpl(config);
    }
}
