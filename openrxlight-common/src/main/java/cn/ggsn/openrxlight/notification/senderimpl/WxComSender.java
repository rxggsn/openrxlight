package cn.ggsn.openrxlight.notification.senderimpl;

import java.util.List;

import cn.ggsn.openrxlight.account.domain.Account;
import cn.ggsn.openrxlight.notification.NotificationReq;
import cn.ggsn.openrxlight.notification.NotificationSender;
import cn.ggsn.openrxlight.notification.domain.ChannelConfiguration;
import cn.ggsn.openrxlight.notification.domain.MessageRecord;
import cn.ggsn.openrxlight.notification.domain.NtyTemplate;
import cn.ggsn.openrxlight.notification.domain.ChannelConfiguration.ChannelType;
import io.quarkus.arc.properties.IfBuildProperty;
import jakarta.enterprise.context.ApplicationScoped;

@IfBuildProperty(name = "rxlight.notification.wxcom.enabled", stringValue = "true")
@ApplicationScoped
class WxComSender implements NotificationSender {

    @Override
    public List<MessageRecord> sendResponse(NotificationReq req, ChannelConfiguration config,
            List<NtyTemplate> templates,
            Account account) {
        throw new UnsupportedOperationException("Unimplemented method 'sendResponse'");
    }

    @Override
    public boolean supports(ChannelType checkChannelType) {
        return ChannelType.WX_COM_CHAT.equals(checkChannelType)
                || ChannelType.WX_COM_ROBOT.equals(checkChannelType);
    }

}
