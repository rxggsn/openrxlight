package cn.ggsn.openrxlight.notification.vender.voice;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VoiceCall {
    private String receiverNumber;
    private Map<String, Object> templateParams;
}
