package cn.ggsn.openrxlight.audio.alibaba;

import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.audio.http_tts.HttpSpeechSynthesisParam;
import com.alibaba.dashscope.audio.http_tts.HttpSpeechSynthesisResult;
import com.alibaba.dashscope.audio.http_tts.HttpSpeechSynthesizer;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.ResultCallback;
import com.alibaba.dashscope.common.Role;
import com.beust.jcommander.internal.Maps;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import cn.ggsn.openrxlight.audio.AudioRecognizer;
import cn.ggsn.openrxlight.lang.Lists2;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AlicloudAudioRecognizerImpl implements AudioRecognizer {

    private static final String SST_MODEL = "qwen3-asr-flash";
    private static final String TTS_MODEL = "cosyvoice-v3-flash";
    private final String apiKey;
    private final String ttsVoice;

    @Slf4j
    static class TTSCallbackHandler extends ResultCallback<HttpSpeechSynthesisResult> {
        private volatile boolean completed = false;
        private final OutputStream outputStream;

        public TTSCallbackHandler(OutputStream outputStream) {
            this.outputStream = outputStream;
        }

        @Override
        public void onEvent(HttpSpeechSynthesisResult message) {
            try {
                outputStream.write(message.getAudioData());
            } catch (Exception e) {
                log.error("Error writing audio data", e);
            }
        }

        @Override
        public void onComplete() {
            this.completed = true;
        }

        @Override
        public void onError(Exception e) {
            log.error("TTS synthesis error", e);
            this.completed = true;
        }

    }

    public AlicloudAudioRecognizerImpl(AlicloudAudioRecognitionConfig config) {
        // QwenTtsRealtimeParam.builder()
        // .model(TTS_MODEL)
        // .apikey(config.apiKey())
        // .url(config.ttsUrl())
        // .build();
        // QwenTtsRealtimeConfig.builder()
        // .voice(config.ttsVoice())
        // .responseFormat(QwenTtsRealtimeAudioFormat.PCM_24000HZ_MONO_16BIT)
        // .mode("commit")
        // // 如需使用指令控制功能，请取消下方注释，并将model替换为qwen3-tts-instruct-flash-realtime
        // // .instructions("")
        // // .optimizeInstructions(true)
        // .build();
        this.apiKey = config.apiKey();
        this.ttsVoice = config.ttsVoice();
    }

    @Override
    public String speechToText(String audioUrl) throws Exception {
        com.alibaba.dashscope.utils.Constants.baseHttpApiUrl = "https://dashscope.aliyuncs.com/api/v1";
        MultiModalConversation conv = new MultiModalConversation();
        MultiModalMessage userMessage = MultiModalMessage.builder()
                .role(Role.USER.getValue())
                .content(Lists2.of(Collections.singletonMap("audio", audioUrl)))
                .build();

        Map<String, Object> asrOptions = Maps.newHashMap();
        asrOptions.put("enable_itn", false);
        // asrOptions.put("language", "zh"); // 可选，若已知音频的语种，可通过该参数指定待识别语种，以提升识别准确率
        MultiModalConversationParam param = MultiModalConversationParam.builder()
                .apiKey(this.apiKey)
                .model(SST_MODEL)
                .message(userMessage)
                .parameter("asr_options", asrOptions)
                .build();
        return conv.streamCall(param).blockingLast().getOutput().getChoices().stream()
                .map(choice -> choice.getMessage().getContent().stream().filter(item -> item.containsKey("text"))
                        .map(item -> (String) item.get("text"))
                        .filter(Objects::nonNull)
                        .filter(StringUtils::isNotBlank)
                        .collect(Collectors.joining("")))
                .collect(Collectors.joining(""));
        // TranscriptionParam param = TranscriptionParam.builder()
        // .apiKey(this.apiKey)
        // .model(SST_MODEL)
        // .fileUrls(Lists2.of(audioUrl))
        // .build();

        // Transcription transcription = new Transcription();
        // TranscriptionResult result = transcription.asyncCall(param);
        // result = transcription.wait(
        // TranscriptionQueryParam.FromTranscriptionParam(param, result.getTaskId()));

        // TranscriptionTaskResult subTaskResult = Lists2.first(result.getResults());
        // String transcriptionResultUrl =
        // this.resolveTranscriptionResultUrl(subTaskResult);
        // try (Response response = this.client
        // .newCall(new
        // Request.Builder().get().url(transcriptionResultUrl).build()).execute()) {
        // if (!response.isSuccessful()) {
        // throw new RuntimeException(
        // "Failed to download transcription result, code: " + response.code() + ",
        // message: "
        // + response.message());
        // }

        // TranscriptionPayload payload =
        // JsonUtils.fromJson(response.body().byteStream(), TranscriptionPayload.class);
        // if (payload == null) {
        // throw new RuntimeException("Failed to parse transcription result");
        // }
        // return payload.extractText();
        // }
    }

    @Override
    public void textToSpeech(AudioRecognizer.TextToSpeechRequest request) throws Exception {
        HttpSpeechSynthesizer synthesizer = new HttpSpeechSynthesizer();
        HttpSpeechSynthesisParam param = HttpSpeechSynthesisParam.builder()
                .model(TTS_MODEL) // 更换模型时，需同步更换为对应版本的音色
                .text(request.getText())
                .voice(this.ttsVoice)
                .format(request.getAudioType().getCode())
                .sampleRate(request.getSampleRate())
                .apiKey(this.apiKey)
                // 通过parameter方法设置额外参数
                .parameter("language_hints", Lists2.of(request.getI18n()))
                // .parameter("enable_ssml", true)
                .build();

        TTSCallbackHandler callback = new TTSCallbackHandler(request.getOutputStream());
        synthesizer.streamCall(param, callback);
        while (!callback.completed) {
            Thread.sleep(100);
        }

        callback.outputStream.flush();

        // TTSCallbackHandler callback = new TTSCallbackHandler();
        // QwenTtsRealtime qwenTtsRealtime = new QwenTtsRealtime(this.ttsParam,
        // callback);

        // qwenTtsRealtime.connect();
        // qwenTtsRealtime.updateSession(this.ttsConfig);

        // qwenTtsRealtime.appendText(text);
        // qwenTtsRealtime.commit();

        // while (!callback.completed) {
        // Thread.sleep(100);
        // }

        // return AudioData.builder()
        // .data(callback.result.flip())
        // .type(audioType)
        // .durationMs((int) (callback.endMs - callback.startMs))
        // .build();
    }

    // private String resolveTranscriptionResultUrl(TranscriptionTaskResult
    // subTaskResult) {
    // if (subTaskResult == null) {
    // throw new IllegalArgumentException("transcription task result is required");
    // }

    // for (String getterName : List.of("getTranscriptionUrl", "getFileUrl",
    // "getResultUrl", "getUrl")) {
    // String value = this.invokeStringGetter(subTaskResult, getterName);
    // if (value != null && !value.isBlank()) {
    // return value;
    // }
    // }

    // throw new IllegalStateException("Unable to resolve transcription result url
    // from task result");
    // }

    // private String invokeStringGetter(Object target, String getterName) {
    // try {
    // Method method = target.getClass().getMethod(getterName);
    // Object value = method.invoke(target);
    // return value instanceof String ? (String) value : null;
    // } catch (ReflectiveOperationException ex) {
    // return null;
    // }
    // }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    static class TranscriptionPayload {
        private List<Transcript> transcripts;

        public String extractText() {
            if (this.transcripts == null || this.transcripts.isEmpty()) {
                return null;
            }

            return this.transcripts.stream()
                    .map(Transcript::getText)
                    .filter(Objects::nonNull)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.joining(""));
        }
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    static class Transcript {
        private Integer channelId;
        private Integer contentDurationInMilliseconds;
        private String text;
        private List<Sentence> sentences;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    static class Sentence {
        private Integer beginTime;
        private Integer endTime;
        private String text;
        private Integer sentenceId;
        private Integer speakerId;
        private List<Word> words;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    static class Word {
        private Integer beginTime;
        private Integer endTime;
        private String text;
        private String punctuation;
    }
}
