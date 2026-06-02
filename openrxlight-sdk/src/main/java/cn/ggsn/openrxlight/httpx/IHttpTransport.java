package cn.ggsn.openrxlight.httpx;

import java.util.stream.Stream;

public interface IHttpTransport {
    RawResponse execute(RawRequest request) throws Exception;

    <T> Stream<T> executeSse(RawRequest request, Class<T> clazz) throws Exception;
}
