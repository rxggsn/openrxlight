package cn.ggsn.openrxlight.notification.senderimpl;

import java.util.List;

import cn.ggsn.openrxlight.account.domain.Account;
import cn.ggsn.openrxlight.notification.NotificationReq;
import cn.ggsn.openrxlight.notification.NotificationSender;
import cn.ggsn.openrxlight.notification.domain.ChannelConfiguration;
import cn.ggsn.openrxlight.notification.domain.MessageRecord;
import cn.ggsn.openrxlight.notification.domain.NtyTemplate;
import cn.ggsn.openrxlight.notification.domain.ChannelConfiguration.ChannelType;
import cn.ggsn.openrxlight.notification.vender.voice.VoiceCall;
import cn.ggsn.openrxlight.notification.vender.voice.VoiceVender;
import io.quarkus.arc.properties.IfBuildProperty;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@IfBuildProperty(name = "rxlight.notification.voice.enabled", stringValue = "true")
@ApplicationScoped
@Slf4j
@RequiredArgsConstructor
class PhoneVoiceSender implements NotificationSender {

    private final VoiceVender voiceVender;

    @Override
    public List<MessageRecord> sendResponse(NotificationReq req, ChannelConfiguration config,
            List<NtyTemplate> templates,
            Account account) {
        try {
            this.voiceVender.send(VoiceCall.builder()
                    .receiverNumber(null)
                    .build());
        } catch (Exception e) {
            log.error("Failed to send phone voice notification to account {}",
                    account.getAccountId(), e);
        }
        return null;
    }

    @Override
    public boolean supports(ChannelType checkChannelType) {
        return ChannelType.PHONE_VOICE.equals(checkChannelType);
    }

    @Override
    public List<MessageRecord> batchSendResponse(NotificationReq notificationReq, ChannelConfiguration config,
            List<NtyTemplate> list, List<Account> accounts) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'batchSendResponse'");
    }

}
