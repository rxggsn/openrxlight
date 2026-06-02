package cn.ggsn.openrxlight.event;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executor;
import javax.annotation.Nonnull;

import com.google.common.collect.Sets;

import lombok.Getter;

public class SubjectConsumer {
    private final com.google.common.eventbus.AsyncEventBus eventBus;
    @Getter(lombok.AccessLevel.PROTECTED)
    private final Class<?> eventType;
    @Getter(lombok.AccessLevel.PROTECTED)
    private final String subject;
    @Getter(lombok.AccessLevel.PROTECTED)
    private final Set<String> dataschemas;

    public SubjectConsumer(@Nonnull String subject,
            @Nonnull Class<?> eventType,
            @Nonnull Executor executor) {
        this.eventBus = new com.google.common.eventbus.AsyncEventBus(
                Objects.requireNonNull(String.format("%sEventBus", subject)),
                executor);
        this.eventType = eventType;
        this.subject = subject;
        this.dataschemas = Sets.newHashSet();
    }

    public void register(String dataschema, @Nonnull Processor listener) {
        if (!this.dataschemas.contains(dataschema)) {
            this.eventBus.register(listener);
        }
    }

    public void post(@Nonnull Object event) {
        this.eventBus.post(event);
    }
}
