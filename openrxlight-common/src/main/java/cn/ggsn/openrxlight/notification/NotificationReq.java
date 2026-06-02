package cn.ggsn.openrxlight.notification;

import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;

import cn.ggsn.openrxlight.notification.domain.NtySceneType;
import cn.ggsn.openrxlight.request.Required;
import cn.ggsn.openrxlight.request.Validate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationReq implements Validate {
    @Required
    private JsonNode content;
    private List<String> attachments;
    @Required
    private NtySceneType sceneType;
}
