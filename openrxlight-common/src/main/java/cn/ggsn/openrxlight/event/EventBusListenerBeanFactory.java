package cn.ggsn.openrxlight.event;

import java.lang.reflect.Method;
import java.util.Objects;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.NormalScope;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Factory to scan beans for methods annotated with @EventBusListener and
 * register them with the appropriate EventBus.
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
class EventBusListenerBeanFactory {

    private final EventBusPublisher publisher;

    void onStartup(@Observes StartupEvent event) {
        log.info("Application StartupEvent observed, initializing EventBus listeners.");
        BeanManager beanManager = CDI.current().getBeanManager();
        for (var bean : beanManager.getBeans(Object.class)) {
            if (!bean.getScope().isAnnotationPresent(NormalScope.class)) {
                continue;
            }
            Object reference = beanManager.getReference(bean, Object.class, beanManager.createCreationalContext(bean));
            Class<?> beanClass = bean.getBeanClass();
            for (Method method : beanClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(EventBusListener.class)) {
                    checkAndProcessMethod(reference, beanClass, method);
                }
            }
        }
    }

    private void checkAndProcessMethod(Object bean, Class<?> targetClass, Method method) {
        EventBusListener annotation = method.getAnnotation(EventBusListener.class);
        if (Objects.nonNull(annotation)) {
            EventBusType type = annotation.type();
            String[] subjects = annotation.topics();
            String dataschema = annotation.groupId();
            for (String subject : subjects) {
                this.publisher.addTarget(type, subject, dataschema, annotation.dataType(), bean, method);
                log.info(
                        "listener {}:{} has been succesffully registered to eventbus {}, topics {}, groupId {}, dataType {}",
                        targetClass.getSimpleName(), method.getName(), type,
                        String.join(", ", subjects), dataschema, annotation.dataType());
            }
        }
    }
}
