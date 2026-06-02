package cn.ggsn.openrxlight.audio;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AudioType {
    PCM("pcm", "Pulse-code modulation"),
    WAV("wav", "Waveform Audio File Format"),
    MP3("mp3", "MPEG Audio Layer III"),
    AAC("aac", "Advanced Audio Coding"),
    OPUS("opus", "Opus audio codec"),;

    private final String code;
    private final String description;

    public static AudioType getFileType(String filepath) {
        String lowerPath = filepath.toLowerCase();
        for (AudioType type : AudioType.values()) {
            if (lowerPath.endsWith("." + type.code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unsupported audio format: " + filepath);
    }
}
