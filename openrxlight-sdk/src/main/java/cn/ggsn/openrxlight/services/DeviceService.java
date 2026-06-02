package cn.ggsn.openrxlight.services;

import cn.ggsn.openrxlight.config.Config;
import cn.ggsn.openrxlight.httpx.HttpMethod;
import cn.ggsn.openrxlight.model.device.DeviceInfo;
import cn.ggsn.openrxlight.request.devices.GetDeviceRequest;
import cn.ggsn.openrxlight.request.devices.ListDeviceRequest;
import cn.ggsn.openrxlight.response.PageResult;
import cn.ggsn.openrxlight.utils.Transport;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeviceService {
    private final Config config;

    public PageResult<DeviceInfo> list(ListDeviceRequest request) throws Exception {
        return Transport.send(this.config, null,
                String.format("/devices/list?%s", request.getQueryParams()),
                HttpMethod.GET.getName(), null)
                .getPageResult(DeviceInfo.class, this.config);
    }

    public DeviceInfo getDeviceInfo(GetDeviceRequest request) throws Exception {
        return Transport.send(this.config, null,
                String.format("/devices/%d", request.getDeviceId()),
                HttpMethod.GET.getName(), request.getQueryParams())
                .getBody(DeviceInfo.class, this.config);
    }
}
