package cn.ggsn.openrxlight.web;

import java.util.UUID;

public interface Command<T> {
    T toResource();

    default T toResourceWithId(String id) {
        return null;
    }

    default T toResourceWithUuId(UUID id) {
        return null;
    }
}
