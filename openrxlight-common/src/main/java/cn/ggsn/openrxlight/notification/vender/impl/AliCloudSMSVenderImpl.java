package cn.ggsn.openrxlight.notification.vender.impl;

import org.apache.commons.lang.StringUtils;

import com.aliyun.credentials.provider.EnvironmentVariableCredentialsProvider;
import com.aliyun.credentials.provider.SystemPropertiesCredentialsProvider;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.tea.TeaException;

import cn.ggsn.openrxlight.lang.Lists2;
import cn.ggsn.openrxlight.notification.domain.MessageStatus;
import cn.ggsn.openrxlight.notification.domain.ChannelConfiguration.ChannelType;
import cn.ggsn.openrxlight.notification.domain.channel.sms.AliSmsAccount;
import cn.ggsn.openrxlight.notification.domain.model.SmsContentModel;
import cn.ggsn.openrxlight.notification.vender.sms.SmsMessage;
import cn.ggsn.openrxlight.notification.vender.sms.SmsResponse;
import cn.ggsn.openrxlight.notification.vender.sms.SmsVender;
import cn.ggsn.openrxlight.utils.JsonUtils;
import io.quarkus.arc.properties.IfBuildProperty;
import io.quarkus.runtime.LaunchMode;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@IfBuildProperty(name = "rxlight.notification.sms.vender", stringValue = "alicloud", enableIfMissing = false)
@Slf4j
@Singleton
@RequiredArgsConstructor
public class AliCloudSMSVenderImpl implements SmsVender {
    private com.aliyun.dysmsapi20170525.Client client;
    private final LaunchMode launchMode;

    @Override
    public SmsResponse send(SmsMessage smsMessage) throws Exception {
        AliSmsAccount account = Lists2
                .filter(smsMessage.getChannel().getAccounts(AliSmsAccount.class),
                        acc -> acc.supports(smsMessage.getTemplate().checkSceneType()))
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No valid AliSmsAccount found in channel configuration for SMS message: " + smsMessage));
        this.init(account);

        var model = (SmsContentModel) JsonUtils.fromJson(smsMessage.getParams(),
                ChannelType.fromCode(smsMessage.getChannel().getType()).getContentModel());
        com.aliyun.dysmsapi20170525.models.SendSmsRequest sendSmsRequest = new com.aliyun.dysmsapi20170525.models.SendSmsRequest()
                .setPhoneNumbers(StringUtils.join(smsMessage.getReceiverNumbers(), ","))
                .setSignName(account.getSignName())
                .setTemplateCode(account.getTemplateCode())
                .setTemplateParam(model.getParams() != null ? model.getParams().toPrettyString() : null);
        try {
            SendSmsResponse sendSms = this.client.sendSms(sendSmsRequest);
            String response = JsonUtils.toJson(sendSms);
            if (sendSms.statusCode != 200 || !"OK".equals(sendSms.body.code)) {
                log.error("Failed to send SMS via AliCloud. Request: {}, Response: {}", sendSmsRequest, response);
                return SmsResponse.builder()
                        .status(MessageStatus.SEND_FAIL)
                        .response(response)
                        .build();
            }
            return SmsResponse.builder()
                    .status(MessageStatus.SEND_SUCCESS)
                    .response(response)
                    .build();
        } catch (TeaException e) {
            if (StringUtils.equals(e.code, "isv.BUSINESS_LIMIT_CONTROL")) {
                log.warn("AliCloud SMS send failed due to biz limit. Request: {}, Error: {}", sendSmsRequest, e);
            } else if (StringUtils.equals(e.code, "isv.DAY_LIMIT_CONTROL")
                    || StringUtils.equals(e.code, "isv.MONTH_LIMIT_CONTROL")) {
                // 日发送量超限，记录日志后丢弃
                log.warn("AliCloud SMS send failed due to daily/monthly limit. Request: {}, Error: {}", sendSmsRequest,
                        e);
            } else if (StringUtils.equals(e.code, "isv.ACCOUNT_NOT_EXISTS")) {
                // 账号不存在，记录日志后丢弃
                log.warn("AliCloud SMS send failed due to account not exists. Request: {}, Error: {}", sendSmsRequest,
                        e);
            } else if (StringUtils.equals(e.code, "isv.INVALID_PARAMETERS")) {
                // 参数错误，记录日志后丢弃
                log.error("AliCloud SMS send failed due to invalid parameters. Request: {}, Error: {}", sendSmsRequest,
                        e);
            } else if (StringUtils.equals(e.code, "isv.SMS_CONTENT_ILLEGAL")) {
                // 短信内容非法，记录日志后丢弃
                log.warn("AliCloud SMS send failed due to illegal SMS content. Request: {}, Error: {}", sendSmsRequest,
                        e);
            } else if (StringUtils.equals(e.code, "isv.MOBILE_NUMBER_ILLEGAL")) {
                // 手机号码非法，记录日志后丢弃
                log.warn("AliCloud SMS send failed due to illegal mobile number. Request: {}, Error: {}",
                        sendSmsRequest, e);
            } else {
                log.error("Failed to send SMS via AliCloud. Request: {}, Error: {}", sendSmsRequest, e);
            }

            return SmsResponse.builder().status(MessageStatus.SEND_FAIL)
                    .response(e.message).build();
        } catch (Exception e) {
            log.error("Failed to send SMS via AliCloud. Request: {}, Error: {}", sendSmsRequest, e);
            throw e;
        }
    }

    private void init(AliSmsAccount account) throws Exception {
        if (this.client == null) {
            com.aliyun.credentials.Client credential = new com.aliyun.credentials.Client(
                    LaunchMode.DEVELOPMENT.equals(this.launchMode) ? new SystemPropertiesCredentialsProvider()
                            : new EnvironmentVariableCredentialsProvider());
            com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
                    .setCredential(credential);
            config.endpoint = account.getEndpoint();
            this.client = new com.aliyun.dysmsapi20170525.Client(config);
        }
    }
}
