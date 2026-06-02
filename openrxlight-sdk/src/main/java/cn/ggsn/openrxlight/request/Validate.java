package cn.ggsn.openrxlight.request;

import org.apache.commons.lang.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

public interface Validate {

    @SuppressWarnings({ "rawtypes" })
    default void validate() {
        for (Field field : this.getClass().getDeclaredFields()) {
            for (Annotation annotation : field.getAnnotations()) {
                if (annotation instanceof Required) {
                    try {
                        field.setAccessible(true);
                        Object o = field.get(this);
                        if (Objects.isNull(o)) {
                            throw new IllegalArgumentException(String.format("field %s is required", field.getName()));
                        }

                        if (o instanceof String) {
                            if (StringUtils.isBlank(StringUtils.trim((String) o))) {
                                throw new IllegalArgumentException(
                                        String.format("field %s is required", field.getName()));
                            }
                        }

                        if (o instanceof Integer) {
                            if ((Integer) o == 0) {
                                throw new IllegalArgumentException(
                                        String.format("field %s is required", field.getName()));
                            }
                        }

                        if (o instanceof List) {
                            if (((List) o).isEmpty()) {
                                throw new IllegalArgumentException(
                                        String.format("field %s is required", field.getName()));
                            }
                        }

                        if (o instanceof Validate) {
                            ((Validate) o).validate();
                        }
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }
}
