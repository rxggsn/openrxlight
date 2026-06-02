package cn.ggsn.openrxlight.services;

import cn.ggsn.openrxlight.config.Config;
import cn.ggsn.openrxlight.httpx.HttpMethod;
import cn.ggsn.openrxlight.model.order.OpsOrder;
import cn.ggsn.openrxlight.model.station.QueueInfo;
import cn.ggsn.openrxlight.request.DatePageReq;
import cn.ggsn.openrxlight.request.orders.CreateOrderRequest;
import cn.ggsn.openrxlight.request.orders.CreateReservationRequest;
import cn.ggsn.openrxlight.request.orders.GetOrderRequest;
import cn.ggsn.openrxlight.request.orders.GetQueueInfoRequest;
import cn.ggsn.openrxlight.request.orders.ModifyReservationRequest;
import cn.ggsn.openrxlight.response.PageResult;
import cn.ggsn.openrxlight.response.orders.CreateOrderResponse;
import cn.ggsn.openrxlight.response.orders.CreateReservationResponse;
import cn.ggsn.openrxlight.response.orders.FinishOrderResponse;
import cn.ggsn.openrxlight.response.orders.ModifyReservationResponse;
import cn.ggsn.openrxlight.utils.Transport;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrderService {

    private final Config config;

    public QueueInfo queueInfo(GetQueueInfoRequest request) throws Exception {
        return Transport.send(this.config, null,
                String.format("/orders/queue_info/%d/%d", request.getStationId(), request.getRangeId()),
                HttpMethod.GET.getName(), null)
                .getBody(QueueInfo.class, this.config);
    }

    public CreateOrderResponse createOrder(CreateOrderRequest request) throws Exception {
        return Transport.send(this.config, request,
                "/orders/create",
                HttpMethod.POST.getName(), null)
                .getBody(CreateOrderResponse.class, this.config);
    }

    public FinishOrderResponse finishOrder(GetOrderRequest request) throws Exception {
        return Transport.send(this.config, null,
                String.format("/orders/%s", request.getMerchantOrderNo()),
                HttpMethod.DELETE.getName(), null)
                .getBody(FinishOrderResponse.class, this.config);
    }

    public OpsOrder getOrder(GetOrderRequest request) throws Exception {
        return Transport.send(this.config, null,
                String.format("/orders/%s", request.getMerchantOrderNo()),
                HttpMethod.GET.getName(), null)
                .getBody(OpsOrder.class, this.config);
    }

    public PageResult<OpsOrder> list(DatePageReq request) throws Exception {
        return Transport.send(this.config, null,
                "/orders/list",
                HttpMethod.GET.getName(), request.getQueryParams())
                .getPageResult(OpsOrder.class, this.config);
    }

    public CreateReservationResponse createReservation(CreateReservationRequest request) throws Exception {
        return Transport.send(this.config, request,
                "/orders/reservation",
                HttpMethod.POST.getName(), null)
                .getBody(CreateReservationResponse.class, this.config);
    }

    public ModifyReservationResponse modifyReservation(ModifyReservationRequest modifyReservationRequest)
            throws Exception {
        return Transport.send(this.config, modifyReservationRequest,
                "/orders/reservation",
                HttpMethod.PATCH.getName(), null)
                .getBody(ModifyReservationResponse.class, this.config);
    }
}
