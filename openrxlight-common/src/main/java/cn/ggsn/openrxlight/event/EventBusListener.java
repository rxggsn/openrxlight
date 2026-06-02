package cn.ggsn.openrxlight.event;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EventBusListener {
    /**
     * Topic name
     * Kafka: topic name
     * Redis: stream name
     * Local: not used
     */
    String[] topics() default "";

    /**
     * Group name
     * Kafka: consumer group
     * Redis: consumer group
     * Local: not used
     */
    String groupId() default "";

    /**
     * Consumer name
     * Kafka: not used
     * Redis: consumer name
     * Local: class name
     */
    String source() default "";

    /**
     * Event bus type
     * Default is local event bus
     */
    EventBusType type() default EventBusType.LOCAL;

    /**
     * Data type class that the listener method processes
     * 
     * @return
     */
    Class<?> dataType() default Object.class;
}
