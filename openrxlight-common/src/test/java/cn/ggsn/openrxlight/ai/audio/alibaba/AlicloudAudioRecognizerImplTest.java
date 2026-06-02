package cn.ggsn.openrxlight.ai.audio.alibaba;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cn.ggsn.openrxlight.audio.alibaba.AlicloudAudioRecognitionConfig;
import cn.ggsn.openrxlight.audio.alibaba.AlicloudAudioRecognizerImpl;

public class AlicloudAudioRecognizerImplTest {

    private static class TestConfig implements AlicloudAudioRecognitionConfig {

        @Override
        public String apiKey() {
            return System.getenv("DASHSCOPE_API_KEY");
        }

        @Override
        public String ttsVoice() {
            return "xiaoyun";
        }
    }

    private AlicloudAudioRecognizerImpl recognizer;

    @BeforeEach
    void setup() {
        recognizer = new AlicloudAudioRecognizerImpl(new TestConfig());
    }

    @Test
    void testSpeechToText() throws Exception {
        String text = this.recognizer.speechToText(
                "https://rxdomain-dev.oss-cn-hangzhou.aliyuncs.com/dhforce_ai/examples/audio/example_zh.wav");
        Assertions.assertEquals("我还能再搞一个，就算是非常小的声音也能识别准确。", text.trim());

        text = this.recognizer.speechToText(
                "https://rxdomain-dev.oss-cn-hangzhou.aliyuncs.com/dhforce_ai/examples/audio/example_en.wav");
        Assertions.assertEquals(
                "Be careful not to allow fabric to become too hot, which can cause shrinkage or, in extreme cases, scorch.",
                text.trim());
    }
}
