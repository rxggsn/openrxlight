package cn.ggsn.openrxlight.notification.vender.sms;

public interface SmsVender {
    SmsResponse send(SmsMessage smsMessage) throws Exception;
}
