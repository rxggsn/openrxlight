package cn.ggsn.rxlight.notification.impl;

import java.util.List;

import cn.ggsn.openrxlight.account.domain.Account;
import cn.ggsn.openrxlight.notification.NotificationReq;
import cn.ggsn.openrxlight.notification.NotificationSender;
import cn.ggsn.openrxlight.notification.domain.ChannelConfiguration;
import cn.ggsn.openrxlight.notification.domain.MessageRecord;
import cn.ggsn.openrxlight.notification.domain.NtyTemplate;
import cn.ggsn.openrxlight.notification.domain.ChannelConfiguration.ChannelType;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EmailNotifier implements NotificationSender {
    @Override
    public List<MessageRecord> sendResponse(NotificationReq req, ChannelConfiguration config,
            List<NtyTemplate> templates, Account account) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'sendResponse'");
    }

    @Override
    public boolean supports(ChannelType checkChannelType) {
        return ChannelType.EMAIL.equals(checkChannelType);
    }

}
