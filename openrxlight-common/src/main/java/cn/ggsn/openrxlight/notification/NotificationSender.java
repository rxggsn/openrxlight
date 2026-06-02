package cn.ggsn.openrxlight.notification;

import java.util.List;

import cn.ggsn.openrxlight.account.domain.Account;
import cn.ggsn.openrxlight.notification.domain.ChannelConfiguration;
import cn.ggsn.openrxlight.notification.domain.MessageRecord;
import cn.ggsn.openrxlight.notification.domain.NtyTemplate;

public interface NotificationSender {

    List<MessageRecord> sendResponse(NotificationReq req,
            ChannelConfiguration config,
            List<NtyTemplate> templates,
            Account account);

    boolean supports(ChannelConfiguration.ChannelType checkChannelType);

}
