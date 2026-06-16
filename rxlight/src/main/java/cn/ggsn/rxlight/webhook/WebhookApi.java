package cn.ggsn.rxlight.webhook;

import java.util.Optional;

import cn.ggsn.openrxlight.event.WebhookEvent;
import cn.ggsn.openrxlight.event.WebhookResponse;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/webhook")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class WebhookApi {
    private final Instance<WebhookEventHandler> webhookEventHandlers;

    public WebhookApi(@Any Instance<WebhookEventHandler> webhookEventHandlers) {
        this.webhookEventHandlers = webhookEventHandlers;
    }

    @POST
    @Path("/")
    public WebhookResponse receiveWebhookEvent(WebhookEvent event) {
        this.selectWebhookEventHandler(event)
                .ifPresent(handler -> handler.handleEvent(event));
        return WebhookResponse.success();
    }

    private Optional<WebhookEventHandler> selectWebhookEventHandler(WebhookEvent event) {
        return this.webhookEventHandlers.stream()
                .filter(handler -> handler.support(event.getEventType()))
                .findFirst();
    }
}
