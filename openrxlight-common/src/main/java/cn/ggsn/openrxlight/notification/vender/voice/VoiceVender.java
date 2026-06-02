package cn.ggsn.openrxlight.notification.vender.voice;

public interface VoiceVender {

    VoiceCallResult send(VoiceCall voiceCall) throws Exception;

}
