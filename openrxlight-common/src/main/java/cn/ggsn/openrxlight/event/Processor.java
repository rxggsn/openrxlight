package cn.ggsn.openrxlight.event;

import java.lang.reflect.Method;
import com.google.common.eventbus.Subscribe;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Processor to handle events from EventBus
 * It's a single-threaded processor per registered method.
 * TODO: Consider making it multi-threaded if needed.
 */
@Slf4j
@RequiredArgsConstructor
public class Processor {

    private final Object bean;
    private final Method method;

    @Subscribe
    public void process(CloudEvent<?> event) {
        log.debug("Processing event: {}", event);
        try {
            method.invoke(bean, event);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke event handler method", e);
        }
    }
}
