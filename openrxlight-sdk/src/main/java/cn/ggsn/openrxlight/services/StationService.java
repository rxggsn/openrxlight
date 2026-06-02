package cn.ggsn.openrxlight.services;

import cn.ggsn.openrxlight.config.Config;
import cn.ggsn.openrxlight.httpx.HttpMethod;
import cn.ggsn.openrxlight.model.station.StationInfo;
import cn.ggsn.openrxlight.model.station.StationSpace;
import cn.ggsn.openrxlight.request.PageRequest;
import cn.ggsn.openrxlight.request.stations.GetStationInfoRequest;
import cn.ggsn.openrxlight.request.stations.GetStationSpaceRequest;
import cn.ggsn.openrxlight.request.stations.ListStationSpacesRequest;
import cn.ggsn.openrxlight.response.PageResult;
import cn.ggsn.openrxlight.utils.Transport;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StationService {
    private final Config config;

    public PageResult<StationInfo> list(PageRequest request) throws Exception {
        return Transport.send(this.config, request, "/stations/list",
                HttpMethod.GET.getName(), null).getPageResult(
                        StationInfo.class, this.config);
    }

    public StationInfo getStationInfo(GetStationInfoRequest request) throws Exception {
        return Transport.send(this.config, null,
                String.format("/stations/%d", request.getStationId()),
                HttpMethod.GET.getName(), null)
                .getBody(StationInfo.class, this.config);
    }

    public PageResult<StationSpace> listStationSpaces(ListStationSpacesRequest request) throws Exception {
        return Transport.send(this.config, null,
                String.format("/stations/%d/spaces/list", request.getStationId()),
                HttpMethod.GET.getName(), request.getQueryParams())
                .getPageResult(StationSpace.class, this.config);
    }

    public StationSpace getStationSpace(GetStationSpaceRequest request) throws Exception {
        return Transport.send(this.config, null,
                String.format("/stations/%d/spaces/%d", request.getStationId(), request.getSpaceId()),
                HttpMethod.GET.getName(), null)
                .getBody(StationSpace.class, this.config);
    }
}
