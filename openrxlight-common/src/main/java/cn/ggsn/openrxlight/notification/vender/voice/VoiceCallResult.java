package cn.ggsn.openrxlight.notification.vender.voice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoiceCallResult {
    private boolean success;
    private String message;
}
