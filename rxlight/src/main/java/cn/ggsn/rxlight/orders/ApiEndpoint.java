package cn.ggsn.rxlight.orders;

import java.util.Objects;

import cn.ggsn.openrxlight.account.domain.Account;
import cn.ggsn.openrxlight.account.error.AccountError;
import cn.ggsn.openrxlight.errorx.BizException;
import cn.ggsn.openrxlight.errorx.order.OrderErrorCode;
import cn.ggsn.openrxlight.transaction.domain.ChannelType;
import cn.ggsn.openrxlight.transaction.domain.CounterpartyTransaction;
import cn.ggsn.openrxlight.transaction.domain.PaymentCounterpartyAccount;
import cn.ggsn.openrxlight.transaction.domain.Transaction;
import cn.ggsn.openrxlight.transaction.domain.TransactionType;
import cn.ggsn.openrxlight.transaction.error.TpsErrorCode;
import cn.ggsn.openrxlight.transaction.service.payment.PaymentWrapper;
import cn.ggsn.openrxlight.web.AuthorizationToken;
import cn.ggsn.rxlight.orders.domain.RxLightOrder;
import cn.ggsn.rxlight.orders.request.PayForOrderRequest;
import cn.ggsn.rxlight.orders.response.PayForOrderResponse;
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

        @POST
        @Path("/pay")
        public PayForOrderResponse payForOrder(PayForOrderRequest request, @Context SecurityContext securityContext) {
                AuthorizationToken token = (AuthorizationToken) securityContext;
                request.validate();

                RxLightOrder order = RxLightOrder.findById(request.getOrderId());
                if (!Objects.equals(token.getAccountId(), order.getAccountId())) {
                        throw new BizException(OrderErrorCode.NotFoundOrder, request.getOrderId());
                }

                ChannelType channelType = ChannelType.UNSPECIFIED;
                TransactionType transactionType = TransactionType.fromValue(request.getTransactionType());
                switch (transactionType) {
                        case WECHAT_PAY:
                        case WECHAT_REFUND:
                        case WALLET_WITHDRAW:
                        case WALLET_DEPOSIT:
                                channelType = ChannelType.FUIOU_PAY;
                                break;

                        case WALLET_PAY:
                                channelType = ChannelType.WALLET;
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
}
