package cn.ggsn.rxlight.webhook.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import cn.ggsn.openrxlight.account.domain.Account;
import cn.ggsn.openrxlight.account.error.AccountError;
import cn.ggsn.openrxlight.api.OpenRxLightV2;
import cn.ggsn.openrxlight.domain.AccountType;
import cn.ggsn.openrxlight.errorx.BizException;
import cn.ggsn.openrxlight.errorx.order.OrderErrorCode;
import cn.ggsn.openrxlight.event.WebhookEvent;
import cn.ggsn.openrxlight.event.order.OrderInfo;
import cn.ggsn.openrxlight.event.order.OrderStatusChange;
import cn.ggsn.openrxlight.lang.Lists2;
import cn.ggsn.openrxlight.model.Currency;
import cn.ggsn.openrxlight.model.order.OpsOrder.OrderStatus;
import cn.ggsn.openrxlight.notification.NotificationReq;
import cn.ggsn.openrxlight.notification.NotificationService;
import cn.ggsn.openrxlight.notification.domain.NtySceneType;
import cn.ggsn.openrxlight.transaction.domain.Transaction;
import cn.ggsn.openrxlight.transaction.domain.TransactionType;
import cn.ggsn.openrxlight.transaction.error.TpsErrorCode;
import cn.ggsn.openrxlight.transaction.pay.wechat.domain.WechatRefundAdditionalInfo;
import cn.ggsn.openrxlight.transaction.service.payment.PaymentWrapper;
import cn.ggsn.openrxlight.transaction.vo.TransactionInfo;
import cn.ggsn.rxlight.orders.domain.RxLightOrder;
import cn.ggsn.rxlight.orders.domain.RxLightOrder.AdditionalInfo;
import cn.ggsn.rxlight.webhook.WebhookEventHandler;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
@RequiredArgsConstructor
class OrderWebhookHandler implements WebhookEventHandler {

        private final OpenRxLightV2 openRxLightV2;
        private final PaymentWrapper paymentWrapper;
        private final NotificationService notificationService;

        @Override
        public void handleEvent(WebhookEvent event) {
                try {
                        NotificationReq notificationReq = NotificationReq.builder().build();
                        Account account = null;
                        if (WebhookEvent.Type.ORDER_STATUS_CHANGED == event.getEventType()) {
                                OrderStatusChange statusChange = event.getBody(OrderStatusChange.class,
                                                this.openRxLightV2.getConfig());
                                log.info(".............receive status change event {}...................",
                                                statusChange);
                                var order = RxLightOrder.getByOrderNo(statusChange.getMerchantOrderNo())
                                                .orElseThrow(() -> new BizException(OrderErrorCode.NotFoundOrder,
                                                                statusChange.getMerchantOrderNo()));
                                if (OrderStatus.CHARGING.equals(OrderStatus.fromValue(statusChange.getStatus()))
                                                && order.getStatus() < OrderStatus.CHARGING.getValue()) {
                                        account = Account.getByAccountId(order.getAccountId(), AccountType.CONSUMER)
                                                        .orElseThrow(() -> new BizException(
                                                                        AccountError.AccountNotExist,
                                                                        order.getAccountId().toString()));
                                        notificationReq.setSceneType(NtySceneType.ORDER_START_CHARGE);
                                        notificationReq.setContent(JsonNodeFactory.instance.textNode("您的充电订单开始服务"));

                                }
                                order.updateStatus(OrderStatus.fromValue(statusChange.getStatus()));

                        } else if (WebhookEvent.Type.ORDER_SETTLED == event.getEventType()) {
                                OrderInfo orderInfo = event.getBody(OrderInfo.class, this.openRxLightV2.getConfig());

                                var order = RxLightOrder.getByOrderNo(orderInfo.getMerchantOrderNo())
                                                .orElseThrow(() -> new BizException(OrderErrorCode.NotFoundOrder,
                                                                orderInfo.getMerchantOrderNo()));
                                account = Account.getByAccountId(order.getAccountId(), AccountType.CONSUMER)
                                                .orElseThrow(() -> new BizException(
                                                                AccountError.AccountNotExist,
                                                                order.getAccountId().toString()));
                                order.setChargeCcy(orderInfo.getChargeCcy());
                                order.setChargedPower(orderInfo.getChargePower());
                                order.setStatus(OrderStatus.FINISH.getValue());
                                order.setTotalCcy(orderInfo.getTotalCcy());
                                order.setChargeStartTime(orderInfo.getChargeStartTime());
                                order.setChargeEndTime(orderInfo.getChargeEndTime());
                                order.setUpdatedTime(LocalDateTime.now());
                                order.setAdditionalInfo(
                                                AdditionalInfo.builder().deviceIds(Lists2.of(orderInfo.getDeviceId()))
                                                                .stopReason(orderInfo.getStopReason()).build());
                                order.save();
                                Currency refundCcy = Currency.ofCent(
                                                java.util.Currency.getInstance(Locale.CHINA).getCurrencyCode(),
                                                order.getPreFee() - order.getChargeCcy());

                                notificationReq.setSceneType(NtySceneType.ORDER_END_CHARGE);
                                notificationReq.setContent(JsonNodeFactory.instance.textNode(""));
                                if (refundCcy.getAmount().compareTo(BigDecimal.ZERO) == 1) {
                                        var transaction = Transaction.getByTransactionId(order.getTransactionId())
                                                        .orElseThrow(() -> new BizException(
                                                                        TpsErrorCode.TransactionNotFound,
                                                                        order.getTransactionId()));
                                        this.paymentWrapper.doPayment(
                                                        new Transaction(transaction
                                                                        .getTransactionInfo().getSource()
                                                                        .getCounterpartyId(),
                                                                        null, refundCcy,
                                                                        new TransactionInfo(
                                                                                        transaction.getTransactionInfo()
                                                                                                        .getDestination(),
                                                                                        transaction.getTransactionInfo()
                                                                                                        .getSource(),
                                                                                        null),
                                                                        TransactionType.WECHAT_REFUND),
                                                        new WechatRefundAdditionalInfo(transaction.getTransactionId(),
                                                                        transaction.getCcy(),
                                                                        transaction.getChannelTransactionId()));
                                }

                                notificationReq.setContent(JsonNodeFactory.instance.textNode("您的充电订单已完成服务"));

                        }

                        if (Objects.nonNull(account)) {
                                this.notificationService.sendToAccount(notificationReq, account);
                        }
                } catch (RuntimeException e) {
                        throw e;
                } catch (Exception e) {
                        throw new RuntimeException(e);
                }
        }

        @Override
        public boolean support(Integer eventType) {
                return WebhookEvent.Type.ORDER_STATUS_CHANGED == eventType ||
                                WebhookEvent.Type.ORDER_SETTLED == eventType;
        }

}
