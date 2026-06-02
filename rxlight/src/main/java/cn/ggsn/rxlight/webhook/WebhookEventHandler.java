package cn.ggsn.rxlight.webhook;

import cn.ggsn.openrxlight.event.WebhookEvent;

public interface WebhookEventHandler {

    void handleEvent(WebhookEvent event);

    boolean support(Integer eventType);
}
