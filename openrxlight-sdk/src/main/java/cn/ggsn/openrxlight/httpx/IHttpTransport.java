package cn.ggsn.openrxlight.httpx;

import io.reactivex.Flowable;

public interface IHttpTransport {
    RawResponse execute(RawRequest request) throws Exception;

    <T> Flowable<T> executeSse(RawRequest request, Class<T> clazz) throws Exception;
}
