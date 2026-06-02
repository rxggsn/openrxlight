package cn.ggsn.openrxlight.audio.alibaba;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "openrxlight.ai.audio.alicloud")
public interface AlicloudAudioRecognitionConfig {
    String apiKey();

    String ttsVoice();
}
