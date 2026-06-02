package cn.ggsn.openrxlight.notification.vender.sms;

import cn.ggsn.openrxlight.notification.domain.MessageStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SmsResponse {
    private MessageStatus status;
    private String response;
}
