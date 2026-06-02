package cn.ggsn.openrxlight.event;

import lombok.Data;

@Data
public class WebhookResponse {
    private int success;

    public static WebhookResponse success() {
        WebhookResponse webhookResponse = new WebhookResponse();
        webhookResponse.setSuccess(1);
        return webhookResponse;
    }

    public static WebhookResponse failure() {
        WebhookResponse webhookResponse = new WebhookResponse();
        webhookResponse.setSuccess(0);
        return webhookResponse;
    }
}
