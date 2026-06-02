package cn.ggsn.openrxlight.notification.senderimpl;

import java.util.List;

import cn.ggsn.openrxlight.account.domain.Account;
import cn.ggsn.openrxlight.domain.ExternalAccount;
import cn.ggsn.openrxlight.domain.ExternalAccountType;
import cn.ggsn.openrxlight.domain.ExternalAccount.WechatAccountInfo;
import cn.ggsn.openrxlight.lang.Lists2;
import cn.ggsn.openrxlight.notification.NotificationReq;
import cn.ggsn.openrxlight.notification.NotificationSender;
import cn.ggsn.openrxlight.notification.domain.ChannelConfiguration;
import cn.ggsn.openrxlight.notification.domain.MessageRecord;
import cn.ggsn.openrxlight.notification.domain.NtyTemplate;
import cn.ggsn.openrxlight.notification.domain.ChannelConfiguration.ChannelType;
import cn.ggsn.openrxlight.notification.vender.sms.SmsMessage;
import cn.ggsn.openrxlight.notification.vender.sms.SmsVender;
import cn.ggsn.openrxlight.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.quarkus.arc.properties.IfBuildProperty;
import jakarta.enterprise.context.ApplicationScoped;

@IfBuildProperty(name = "rxlight.notification.sms.enabled", stringValue = "true")
@ApplicationScoped
@Slf4j
@RequiredArgsConstructor
class SmsSender implements NotificationSender {

    private final SmsVender smsVender;

    @Override
    public List<MessageRecord> sendResponse(NotificationReq req, ChannelConfiguration config,
            List<NtyTemplate> templates,
            Account account) {
        List<ExternalAccount> externalAccounts = account
                .getExternalAccounts(Lists2.of(ExternalAccountType.PHONE, ExternalAccountType.WECHAT_MINI_PROGRAM));
        var phoneNos = Lists2.mapNotNull(externalAccounts, ea -> {
            String phoneNo = null;
            if (ea.getAccountInfo() != null && ea.getAccountInfo() instanceof WechatAccountInfo) {
                WechatAccountInfo wechatAccountInfo = (WechatAccountInfo) ea.getAccountInfo();
                phoneNo = wechatAccountInfo.getPhoneNo();
            } else if (ExternalAccountType.PHONE.equals(ea.getAccountType())) {
                phoneNo = ea.getExternalAccountId();
            }

            return phoneNo;
        });

        return Lists2.map(templates, template -> {
            try {
                SmsMessage smsMessage = SmsMessage
                        .builder()
                        .receiverNumbers(phoneNos)
                        .params(req.getContent())
                        .channel(config)
                        .template(template)
                        .build();
                var response = this.smsVender.send(smsMessage);
                log.debug("SMS sent with response: {}", response);

                return MessageRecord.builder()
                        .accountId(account.getAccountId())
                        .templateId(template.id)
                        .response(response.getResponse())
                        .content(JsonUtils.toJson(smsMessage))
                        .status(response.getStatus().getCode())
                        .build();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

    }

    @Override
    public boolean supports(ChannelType channelType) {
        return ChannelType.SMS.equals(channelType);
    }

}
