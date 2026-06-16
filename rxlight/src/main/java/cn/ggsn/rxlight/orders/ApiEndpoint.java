package cn.ggsn.rxlight.orders;

import java.util.Currency;
import java.util.Locale;
import java.util.Objects;

import cn.ggsn.openrxlight.account.domain.Account;
import cn.ggsn.openrxlight.account.error.AccountError;
import cn.ggsn.openrxlight.api.OpenRxLightV2;
import cn.ggsn.openrxlight.errorx.BizException;
import cn.ggsn.openrxlight.errorx.order.OrderErrorCode;
import cn.ggsn.openrxlight.model.billing.PaymentChannel;
import cn.ggsn.openrxlight.model.order.PayType;
import cn.ggsn.openrxlight.model.order.OpsOrder.OrderStatus;
import cn.ggsn.openrxlight.model.station.StationSpace;
import cn.ggsn.openrxlight.request.stations.GetStationSpaceRequest;
import cn.ggsn.openrxlight.transaction.domain.ChannelType;
import cn.ggsn.openrxlight.transaction.domain.CounterpartyTransaction;
import cn.ggsn.openrxlight.transaction.domain.PaymentCounterpartyAccount;
import cn.ggsn.openrxlight.transaction.domain.Transaction;
import cn.ggsn.openrxlight.transaction.domain.TransactionType;
import cn.ggsn.openrxlight.transaction.error.TpsErrorCode;
import cn.ggsn.openrxlight.transaction.service.payment.PaymentWrapper;
import cn.ggsn.openrxlight.web.AuthorizationToken;
import cn.ggsn.rxlight.account.domain.ConsumerAccountExtInfo;
import cn.ggsn.rxlight.orders.domain.RxLightOrder;
import cn.ggsn.rxlight.orders.request.CreateOrderRequest;
import cn.ggsn.rxlight.orders.request.PayForOrderRequest;
import cn.ggsn.rxlight.orders.response.CreateOrderResponse;
import cn.ggsn.rxlight.orders.response.PayForOrderResponse;
import cn.ggsn.rxlight.system.Sequence;
import cn.ggsn.openrxlight.transaction.pay.domain.Counterparty;
import cn.ggsn.openrxlight.transaction.pay.wechat.domain.WxNativePayAdditionalInfo;
import cn.ggsn.openrxlight.transaction.vo.TransactionInfo;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;
import lombok.RequiredArgsConstructor;

@Path("/orders")
@RequiredArgsConstructor
public class ApiEndpoint {

        private final PaymentWrapper paymentWrapper;
        private final OpenRxLightV2 openRxLightV2;

        @POST
        @Path("/pay")
        public PayForOrderResponse payForOrder(PayForOrderRequest request, @Context SecurityContext securityContext) {
                AuthorizationToken token = (AuthorizationToken) securityContext;
                request.validate();

                RxLightOrder order = RxLightOrder.getByOrderNo(request.getOrderNo()).orElseThrow(
                                () -> new BizException(OrderErrorCode.NotFoundOrder, request.getOrderNo()));
                if (!Objects.equals(token.getAccountId(), order.getAccountId())) {
                        throw new BizException(OrderErrorCode.NotFoundOrder, request.getOrderNo());
                }

                ChannelType channelType = ChannelType.UNSPECIFIED;
                TransactionType transactionType = TransactionType.UNSPECIFIED;
                var payType = PayType.fromValue(order.getPayType());
                switch ((payType)) {
                        case PRE_FEE:
                        case PWD_FREE:
                                switch (PaymentChannel.fromValue(order.getPaymentChannel())) {
                                        case WECHAT:
                                                channelType = ChannelType.FUIOU_PAY;
                                                transactionType = TransactionType.WECHAT_PAY;
                                        case ALIPAY:
                                                channelType = ChannelType.FUIOU_PAY;
                                }
                                break;

                        default:
                                break;
                }

                var merchantAccount = Account.getById(order.getMerchantId())
                                .orElseThrow(() -> new BizException(AccountError.AccountNotExist,
                                                order.getMerchantId().toString()));
                var counterpartyAccount = PaymentCounterpartyAccount
                                .findByAccountId(merchantAccount.getAccountId(), channelType, order.getCurrencyType())
                                .orElseThrow(() -> new BizException(TpsErrorCode.CounterpartyAccountNotFound,
                                                merchantAccount.getAccountId().toString()));

                Transaction transaction = new Transaction(
                                counterpartyAccount.getAccountId(), null, order.getPrePayFee(),
                                new TransactionInfo(
                                                null,
                                                new Counterparty(counterpartyAccount.getAccountId(),
                                                                counterpartyAccount.getAdditionalInfo()),
                                                order.getOrderNo()),
                                transactionType);
                CounterpartyTransaction counterpartyTxn = this.paymentWrapper.doPayment(transaction,
                                new WxNativePayAdditionalInfo());
                transaction.updateCounterpartyTxnIdAndStatusById(counterpartyTxn.getStatus(),
                                counterpartyTxn.getCounterpartyTxnId(), counterpartyTxn.getChannelTransactionId());
                order.setTransactionId(transaction.getTransactionId());
                order.save();

                String description = "";
                switch (transactionType) {
                        case WECHAT_PAY:
                                var userAccount = Account.getByAccountId(order.getAccountId(), token.getAccountType())
                                                .orElseThrow(
                                                                () -> new BizException(AccountError.AccountNotExist,
                                                                                order.getAccountId().toString()));
                                switch (userAccount.getLanguage()) {
                                        case "zh":
                                                description = "请长按保存二维码并使用微信支付";
                                                break;
                                        default:
                                                description = "Please long press to save the QR code and pay with WeChat";
                                                break;
                                }
                                break;
                        default:
                                break;
                }
                return PayForOrderResponse.builder()
                                .payByQrCode(new PayForOrderResponse.PayByQrCode(
                                                ((CounterpartyTransaction.NativePayInfo) counterpartyTxn
                                                                .getCounterpartyTransactionInfo())
                                                                .getQrCode(),
                                                description))
                                .build();
        }

        @POST
        public CreateOrderResponse createOrder(CreateOrderRequest request, @Context SecurityContext securityContext)
                        throws Exception {
                AuthorizationToken token = (AuthorizationToken) securityContext;
                request.validate();

                var account = Account.getByAccountId(token.getAccountId(), token.getAccountType())
                                .orElseThrow(() -> new BizException(AccountError.AccountNotExist,
                                                token.getAccountId().toString()));
                StationSpace stationSpace = this.openRxLightV2.stations()
                                .getStationSpace(GetStationSpaceRequest.builder()
                                                .spaceId(request.getSpaceId())
                                                .stationId(request.getStationId())
                                                .build());
                var extInfo = (ConsumerAccountExtInfo) account.getAccountInfo();
                var carInfo = extInfo.getCarInfo(request.getPlateNo()).orElseThrow(
                                () -> new BizException(AccountError.NotFoundPlateNo, request.getPlateNo()));
                var order = RxLightOrder.builder()
                                .accountId(token.getAccountId())
                                .orderNo(Sequence.generateCode("order"))
                                .payType((short) request.getPayType().getValue())
                                .preFee(request.getPrepay())
                                .plateNo(request.getPlateNo())
                                .stationId(request.getStationId())
                                .spaceId(request.getSpaceId())
                                .rangeId(stationSpace.getRangeId())
                                .spaceNo(stationSpace.getSpaceNo())
                                .carModelId(carInfo.getModelId() != null ? carInfo.getModelId().toString() : null)
                                .currencyType(Currency.getInstance(Locale.CHINA).getCurrencyCode())
                                .status(OrderStatus.CREATED.getValue())
                                .paymentChannel(request.getPaymentChannel().getValue())
                                .build();
                order.save();
                return CreateOrderResponse.builder().orderNo(order.getOrderNo()).build();
        }
}
