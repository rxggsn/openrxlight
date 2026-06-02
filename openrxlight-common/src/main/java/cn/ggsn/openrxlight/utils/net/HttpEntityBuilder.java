package cn.ggsn.openrxlight.utils.net;

import org.apache.http.HttpEntity;

@FunctionalInterface
public interface HttpEntityBuilder<Req> {
    HttpEntity build(Req req);
}
