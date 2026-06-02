package cn.ggsn.openrxlight.notification.vender.sms;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;

import cn.ggsn.openrxlight.notification.domain.ChannelConfiguration;
import cn.ggsn.openrxlight.notification.domain.NtyTemplate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SmsMessage {
    private List<String> receiverNumbers;
    private JsonNode params;
    @JsonIgnore
    private ChannelConfiguration channel;
    @JsonIgnore
    private NtyTemplate template;
}
