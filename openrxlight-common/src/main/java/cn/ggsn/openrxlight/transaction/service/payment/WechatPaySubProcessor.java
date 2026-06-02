package cn.ggsn.openrxlight.transaction.service.payment;

import org.apache.commons.lang.StringUtils;

import cn.ggsn.openrxlight.errorx.BizException;
import cn.ggsn.openrxlight.transaction.domain.ChannelType;
import cn.ggsn.openrxlight.transaction.domain.CounterpartyTransaction;
import cn.ggsn.openrxlight.transaction.domain.Transaction;
import cn.ggsn.openrxlight.transaction.domain.TransactionStatus;
import cn.ggsn.openrxlight.transaction.domain.TransactionType;
import cn.ggsn.openrxlight.utils.UUIdConverter;
import jakarta.enterprise.inject.Instance;
import cn.ggsn.openrxlight.transaction.pay.WechatPayApi;
import cn.ggsn.openrxlight.transaction.pay.wechat.domain.WechatPayOrder;
import cn.ggsn.openrxlight.transaction.pay.wechat.domain.WechatPayOrderQuery;
import cn.ggsn.openrxlight.transaction.pay.wechat.domain.WechatPrepayAdditionalInfo;
import cn.ggsn.openrxlight.transaction.pay.wechat.domain.WxNativePayAdditionalInfo;
import cn.ggsn.openrxlight.transaction.pay.wechat.error.WechatPayErrorCode;
import cn.ggsn.openrxlight.transaction.pay.wechat.response.WxNativePayResponse;
import cn.ggsn.openrxlight.transaction.vo.Payment;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class WechatPaySubProcessor implements PaymentSubProcessor {
        private final Instance<WechatPayApi> wechatPayApis;

        @Override
        public CounterpartyTransaction process(Payment payment) {
                ChannelType channelType = payment.getTransaction().getTransactionInfo().getDestination()
                                .getCounterpartyAccount().getChannelType();
                WechatPayApi wechatPayApi = this.selectWechatPayApi(payment.getTransaction().checkTransactionType(),
                                channelType);

                if (wechatPayApi == null) {
                        throw new BizException(
                                        WechatPayErrorCode.UnsupportedChannelType, channelType.name());
                }

                if (payment.getTransactionAdditionalInfo() instanceof WxNativePayAdditionalInfo) {
                        WxNativePayResponse nativeResp = wechatPayApi.nativePay(
                                        payment.getRxLightTransactionIdInCounterparty(),
                                        payment.getTransaction().getCcy(),
                                        payment.getTransaction().getTransactionInfo().getDescription(),
                                        payment.getTransaction().getTransactionInfo().getDestination());
                        return new CounterpartyTransaction(
                                        nativeResp.getCounterpartyTxnId(),
                                        CounterpartyTransaction.NativePayInfo
                                                        .builder()
                                                        .qrCode(nativeResp.getQrCode())
                                                        .counterpartyTraceId(nativeResp.getCounterpartyTraceId())
                                                        .build(),
                                        TransactionStatus.INITIALIZED,
                                        nativeResp.getChannelTransactionId());
                } else {
                        var prepay = wechatPayApi.prepay(
                                        payment.getRxLightTransactionIdInCounterparty(),
                                        payment.getTransaction().getCcy(),
                                        payment.getTransaction().getTransactionInfo().getDescription(),
                                        ((WechatPrepayAdditionalInfo) payment.getTransactionAdditionalInfo())
                                                        .getOpenId(),
                                        payment.getTransaction().getTransactionInfo().getDestination());
                        return new CounterpartyTransaction(
                                        prepay.getChannelTransactionId(),
                                        CounterpartyTransaction.WechatPrepayInfo
                                                        .builder()
                                                        .appId(prepay.getAppId())
                                                        .timestamp(prepay.getTimestamp())
                                                        .nonceStr(prepay.getNonceStr())
                                                        .packageValue(prepay.getPackageValue())
                                                        .signType(prepay.getSignType())
                                                        .paySign(prepay.getPaySign())
                                                        .build(),
                                        TransactionStatus.INITIALIZED,
                                        prepay.getChannelTransactionId());
                }

        }

        @Override
        public boolean support(TransactionType transactionType) {
                return TransactionType.WECHAT_PAY.equals(transactionType) ||
                                TransactionType.WALLET_DEPOSIT.equals(transactionType);
        }

        @Override
        public CounterpartyTransaction getCounterpartyTransaction(Transaction transaction) {
                ChannelType channelType = transaction
                                .getTransactionInfo()
                                .getDestination()
                                .getCounterpartyAccount()
                                .getChannelType();
                WechatPayApi wechatPayApi = this.selectWechatPayApi(transaction.checkTransactionType(), channelType);

                if (TransactionType.WECHAT_PAY.equals(transaction.checkTransactionType())) {
                        WechatPayOrder wechatPayOrder = wechatPayApi.queryPayOrder(WechatPayOrderQuery
                                        .builder()
                                        .transactionId(StringUtils.isNotBlank(transaction.getChannelTransactionId())
                                                        ? transaction.getChannelTransactionId()
                                                        : UUIdConverter.replaceHyphenWithEmptyChar(
                                                                        transaction.getTransactionId()))
                                        .build(),
                                        transaction.getTransactionInfo().getDestination());
                        return new CounterpartyTransaction(
                                        transaction.getCounterpartyTxnId(),
                                        new CounterpartyTransaction.WechatPayOrderInfo(wechatPayOrder),
                                        TransactionStatus.fromWechatTradeState(wechatPayOrder.getTradeState()),
                                        wechatPayOrder.getTransactionId());
                }

                return null;
        }

        private WechatPayApi selectWechatPayApi(TransactionType transactionType, ChannelType channelType) {
                return this.wechatPayApis.stream()
                                .filter(wechatPayApi -> wechatPayApi.supports(transactionType, channelType))
                                .findFirst()
                                .orElseThrow(() -> new BizException(WechatPayErrorCode.UnsupportedChannelType,
                                                channelType.name()));
        }
}
