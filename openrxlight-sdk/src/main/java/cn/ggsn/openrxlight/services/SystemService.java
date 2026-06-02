package cn.ggsn.openrxlight.services;

import cn.ggsn.openrxlight.config.Config;
import cn.ggsn.openrxlight.httpx.HttpMethod;
import cn.ggsn.openrxlight.model.system.SystemData;
import cn.ggsn.openrxlight.request.system.GetSystemDataRequest;
import cn.ggsn.openrxlight.utils.Transport;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SystemService {
    private final Config config;

    public SystemData getSystemData(GetSystemDataRequest request) throws Exception {
        return Transport.send(this.config, null,
                "/sysdata",
                HttpMethod.GET.getName(), request.getQueryParams())
                .getBody(SystemData.class, this.config);
    }
}
