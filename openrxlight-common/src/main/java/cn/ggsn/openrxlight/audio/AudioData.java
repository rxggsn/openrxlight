package cn.ggsn.openrxlight.audio;

import java.io.OutputStream;
import java.nio.ByteBuffer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AudioData {
    private ByteBuffer data;
    private AudioType type;
    private int durationMs;
    private OutputStream outputStream;
}
