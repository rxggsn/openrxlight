package cn.ggsn.openrxlight.audio;

import java.io.OutputStream;

import lombok.AllArgsConstructor;
import lombok.Data;

public interface AudioRecognizer {

    @Data
    @AllArgsConstructor
    public static class TextToSpeechRequest {
        private final String text;
        private final AudioType audioType;
        private final String i18n;
        private final int sampleRate;
        private final OutputStream outputStream;
    }

    String speechToText(String audioUrl) throws Exception;

    void textToSpeech(TextToSpeechRequest request) throws Exception;
}
