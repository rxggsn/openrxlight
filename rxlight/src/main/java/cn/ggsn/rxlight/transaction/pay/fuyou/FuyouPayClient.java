package cn.ggsn.rxlight.transaction.pay.fuyou;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

import com.github.binarywang.wxpay.constant.WxPayConstants;
import cn.ggsn.openrxlight.Constants;
import cn.ggsn.openrxlight.errorx.BizException;
import cn.ggsn.openrxlight.lang.Lists2;
import cn.ggsn.openrxlight.lang.Maps2;
import cn.ggsn.openrxlight.model.Currency;
import cn.ggsn.openrxlight.transaction.domain.ChannelType;
import cn.ggsn.openrxlight.transaction.domain.PaymentCounterpartyAccount;
import cn.ggsn.openrxlight.transaction.domain.TransactionType;
import cn.ggsn.openrxlight.utils.UUIdConverter;
import cn.ggsn.openrxlight.utils.XmlUtils;
import cn.ggsn.openrxlight.utils.net.HttpEntityBuilder;
import cn.ggsn.openrxlight.utils.net.NetUtils;
import cn.ggsn.openrxlight.utils.net.XmlRestClient;
import cn.ggsn.openrxlight.transaction.pay.WechatPayApi;
import cn.ggsn.openrxlight.transaction.pay.domain.Counterparty;
import cn.ggsn.rxlight.transaction.pay.fuyou.domain.FuiouCounterpartyAccountInfo;
import cn.ggsn.rxlight.transaction.pay.fuyou.domain.OrderTypeEnum;
import cn.ggsn.rxlight.transaction.pay.fuyou.domain.TradeTypeEnum;
import cn.ggsn.rxlight.transaction.pay.fuyou.request.FuyouSignatureBaseRequest;
import cn.ggsn.rxlight.transaction.pay.fuyou.request.PreCreateRequest;
import cn.ggsn.rxlight.transaction.pay.fuyou.request.PrePayRequest;
import cn.ggsn.rxlight.transaction.pay.fuyou.request.QueryOrderRequest;
import cn.ggsn.rxlight.transaction.pay.fuyou.request.RefundRequest;
import cn.ggsn.rxlight.transaction.pay.fuyou.response.FuyouSignatureBaseResponse;
import cn.ggsn.rxlight.transaction.pay.fuyou.response.PreCreateResponse;
import cn.ggsn.rxlight.transaction.pay.fuyou.response.PrepayResponse;
import cn.ggsn.rxlight.transaction.pay.fuyou.response.QueryOrderResponse;
import cn.ggsn.rxlight.transaction.pay.fuyou.response.RefundResponse;
import cn.ggsn.openrxlight.transaction.pay.wechat.domain.WechatErrorCode;
import cn.ggsn.openrxlight.transaction.pay.wechat.domain.WechatPayOrder;
import cn.ggsn.openrxlight.transaction.pay.wechat.domain.WechatPayOrderQuery;
import cn.ggsn.openrxlight.transaction.pay.wechat.domain.WechatRefund;
import cn.ggsn.openrxlight.transaction.pay.wechat.domain.WechatRefundQuery;
import cn.ggsn.openrxlight.transaction.pay.wechat.response.WechatPrepayResponse;
import cn.ggsn.openrxlight.transaction.pay.wechat.response.WxNativePayResponse;
import cn.hutool.core.lang.generator.SnowflakeGenerator;
import cn.hutool.core.net.url.UrlBuilder;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.vertx.mutiny.redis.client.RedisAPI;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class FuyouPayClient implements WechatPayApi {
        public static final String NODE_ID = "fuyou:pay:nodeId";
        private final XmlRestClient restClient;
        private final List<TransactionType> supportTransactionTypes;

        private final HttpEntityBuilder<FuyouSignatureBaseRequest> ENTITY_BUILDER = req -> {
                String reqValue = XmlUtils.toXml(req, "GBK");

                List<NameValuePair> formParams = Lists2.empty();
                Map<String, String> val = Maps2.of("req", reqValue);

                val.forEach((k, v) -> {
                        BasicNameValuePair valuePair = new BasicNameValuePair(k, v);
                        formParams.add(valuePair);
                });

                try {
                        return new UrlEncodedFormEntity(formParams, "GBK");
                } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                }
        };
        private final SnowflakeGenerator generator;

        @SuppressWarnings("null")
        FuyouPayClient(Instance<RedisAPI> redisApi) {
                List<PaymentCounterpartyAccount> accounts = PaymentCounterpartyAccount
                                .listByChannelType(ChannelType.FUIOU_PAY);
                this.supportTransactionTypes = Lists2
                                .flatMapNotNull(accounts, PaymentCounterpartyAccount::getTransactionTypes)
                                .stream()
                                .map(TransactionType::fromValue)
                                .distinct()
                                .toList();
                this.generator = redisApi.stream()
                                .map(api -> Objects.requireNonNull(api.incr(NODE_ID)
                                                .runSubscriptionOn(Infrastructure.getDefaultExecutor())
                                                .await()
                                                .indefinitely()
                                                .toLong()))
                                .map(nodeId -> new SnowflakeGenerator(nodeId, 0L))
                                .findFirst()
                                .orElseGet(() -> new SnowflakeGenerator(0L, 0L));

                // long nodeId =
                // Objects.requireNonNull(redisApi.opsForValue().increment(NODE_ID));
                this.restClient = XmlRestClient.createDefault();
                this.restClient.setCharset("GBK");
        }

        private String generateOrderId() {
                return Long.toString(this.generator.next());
        }

        @Override
        public WechatPrepayResponse prepay(String orderId,
                        Currency currency,
                        String description,
                        String openId,
                        @NonNull Counterparty counterpartyAccount) {
                PrePayRequest request;
                request = PrePayRequest
                                .builder()
                                .goodsDes(description)
                                .tradeType(TradeTypeEnum.LETPAY)
                                .orderId(orderId)
                                .amount(currency.getAmountInCent().intValue())
                                .notifyUrl(((FuiouCounterpartyAccountInfo) counterpartyAccount
                                                .getCounterpartyAccount())
                                                .getPrepayNotifyUrl())
                                .subOpenId(openId)
                                .subAppId(((FuiouCounterpartyAccountInfo) counterpartyAccount
                                                .getCounterpartyAccount())
                                                .getWxAppId())
                                .createdTime(LocalDateTime.now().format(Constants.QUERY_DATE_TIME_FORMATTER))
                                .currencyType(currency.getCurrencyType())
                                .termIp(NetUtils.getHostAddress())
                                .build();

                if (StringUtils.length(orderId) > 30) {
                        request.setOrderId(StringUtils.join(
                                        ((FuiouCounterpartyAccountInfo) counterpartyAccount
                                                        .getCounterpartyAccount())
                                                        .getOrderPrefix(),
                                        this.generateOrderId()));
                }
                request.setTermId(Consts.TERM_ID);
                this.fillInMerchantCodeAndInsCd(counterpartyAccount, request);
                request.generateRandomStr();

                try {
                        this.fillInSignature(counterpartyAccount, request);
                        PrepayResponse response = this.restClient.post(
                                        UrlBuilder
                                                        .ofHttp(((FuiouCounterpartyAccountInfo) counterpartyAccount
                                                                        .getCounterpartyAccount())
                                                                        .getDomainName())
                                                        .addPath(Consts.PREPAY_URL)
                                                        .build(),
                                        request,
                                        ENTITY_BUILDER,
                                        PrepayResponse.class,
                                        true,
                                        false);

                        if (response.isSuccess()) {
                                this.verifySign(counterpartyAccount, response);
                                // response.verifySign(this.publicKey);
                                return WechatPrepayResponse
                                                .builder()
                                                .paySign(response.getPaySign())
                                                .signType(response.getSignType())
                                                .nonceStr(response.getNonceStr())
                                                .appId(((FuiouCounterpartyAccountInfo) counterpartyAccount
                                                                .getCounterpartyAccount())
                                                                .getWxAppId())
                                                .packageValue(response.getPackageValue())
                                                .timestamp(response.getTimestamp())
                                                .channelTransactionId(request.getOrderId())
                                                .build();
                        } else {
                                throw new BizException(WechatErrorCode.CHANNEL_ERROR, response.getResultCode(),
                                                response.getResultMsg());
                        }
                } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | InvalidKeySpecException
                                | SignatureException e) {
                        throw new RuntimeException(e);
                }
        }

        private void fillInSignature(@NonNull Counterparty account, FuyouSignatureBaseRequest request)
                        throws UnsupportedEncodingException, NoSuchAlgorithmException {
                FuiouCounterpartyAccountInfo additionalInfo = (FuiouCounterpartyAccountInfo) account
                                .getCounterpartyAccount();
                request.generateSign(additionalInfo.generatePrivateKey());
        }

        private void fillInMerchantCodeAndInsCd(@NonNull Counterparty counterpartyAccount,
                        FuyouSignatureBaseRequest request) {
                FuiouCounterpartyAccountInfo additionalInfo = (FuiouCounterpartyAccountInfo) counterpartyAccount
                                .getCounterpartyAccount();
                request.setMerchantCode(additionalInfo.getMchntCd());
                request.setInsCd(additionalInfo.getInsCd());
        }

        private void verifySign(@NonNull Counterparty counterpartyAccount,
                        FuyouSignatureBaseResponse response)
                        throws UnsupportedEncodingException, InvalidKeyException, InvalidKeySpecException,
                        NoSuchAlgorithmException, SignatureException {
                FuiouCounterpartyAccountInfo additionalInfo = (FuiouCounterpartyAccountInfo) counterpartyAccount
                                .getCounterpartyAccount();
                response.verifySign(additionalInfo.getPublicKey());
        }

        @Override
        public WechatRefund refund(String outTradeNo,
                        Currency refundCcy,
                        Currency originCcy,
                        UUID outRefundNo,
                        @NonNull Counterparty counterpartyAccount) {
                RefundRequest request = RefundRequest
                                .builder()
                                .orderType(OrderTypeEnum.WECHAT)
                                .transactionId(outTradeNo)
                                .refundTxnId(UUIdConverter.replaceHyphenWithEmptyChar(outRefundNo))
                                .refundAmount(refundCcy.getAmountInCent().intValue())
                                .totalAmount(originCcy.getAmountInCent().intValue())
                                .build();

                if (StringUtils.length(request.getRefundTxnId()) > 30) {
                        request.setRefundTxnId(StringUtils.join(
                                        ((FuiouCounterpartyAccountInfo) counterpartyAccount.getCounterpartyAccount())
                                                        .getOrderPrefix(),
                                        this.generateOrderId()));
                }
                try {
                        request.setTermId(Consts.TERM_ID);
                        this.fillInMerchantCodeAndInsCd(counterpartyAccount, request);
                        request.generateRandomStr();
                        this.fillInSignature(counterpartyAccount, request);

                        RefundResponse response = this.restClient.post(
                                        UrlBuilder.ofHttp(((FuiouCounterpartyAccountInfo) counterpartyAccount
                                                        .getCounterpartyAccount())
                                                        .getDomainName())
                                                        .addPath(Consts.REFUND_URL)
                                                        .build(),
                                        request,
                                        ENTITY_BUILDER,
                                        RefundResponse.class,
                                        true, false);
                        if (response.isSuccess()) {
                                this.verifySign(counterpartyAccount, response);
                                return WechatRefund
                                                .builder()
                                                .channelRefundId(request.getRefundTxnId())
                                                .systemRefundId(outRefundNo)
                                                .refundId(response.getCounterpartyRefundTxnId())
                                                .wechatTxnId(response.getCounterpartyTxnId())
                                                .rxDomainTxnId(outTradeNo)
                                                .status(WxPayConstants.RefundStatus.SUCCESS)
                                                .counterpartyTraceId(response.getTraceId())
                                                .build();
                        } else {
                                throw new BizException(WechatErrorCode.CHANNEL_ERROR, response.getResultCode(),
                                                response.getResultMsg());
                        }

                } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | InvalidKeySpecException
                                | SignatureException e) {
                        throw new RuntimeException(e);
                }
        }

        @Override
        public WechatRefund queryRefund(WechatRefundQuery query) {
                return null;
        }

        @Override
        public boolean supports(TransactionType transactionType, ChannelType channelType) {
                return ChannelType.FUIOU_PAY.equals(channelType) &&
                                Lists2.contains(this.supportTransactionTypes,
                                                txnType -> txnType.equals(transactionType));
        }

        @Override
        public WechatPayOrder queryPayOrder(WechatPayOrderQuery query, @NonNull Counterparty counterpartyAccount) {
                String transactionId = query.getTransactionId();

                QueryOrderRequest request = QueryOrderRequest
                                .builder()
                                .orderType(OrderTypeEnum.WECHAT)
                                .transactionId(transactionId)
                                .build();
                try {
                        request.setTermId(Consts.TERM_ID);
                        this.fillInMerchantCodeAndInsCd(counterpartyAccount, request);
                        request.generateRandomStr();
                        FuiouCounterpartyAccountInfo accountInfo = (FuiouCounterpartyAccountInfo) counterpartyAccount
                                        .getCounterpartyAccount();
                        this.fillInSignature(counterpartyAccount, request);
                        QueryOrderResponse response = this.restClient.post(
                                        UrlBuilder.ofHttp(accountInfo.getDomainName())
                                                        .addPath(Consts.QUERY_ORDER_URL)
                                                        .build(),
                                        request,
                                        ENTITY_BUILDER,
                                        QueryOrderResponse.class,
                                        true, false);

                        if (response.isSuccess()) {
                                this.verifySign(counterpartyAccount, response);
                                // response.verifySign(this.publicKey);
                                return WechatPayOrder.builder()
                                                .tradeType(com.wechat.pay.java.service.payments.model.Transaction.TradeTypeEnum.JSAPI)
                                                .counterpartyTxnId(response.getCounterpartyTxnId())
                                                .tradeState(response.getTradeState())
                                                .payer(WechatPayOrder.Payer.builder()
                                                                .openId(response.getBuyerId())
                                                                .build())
                                                .transactionId(response.getTransactionId())
                                                .counterpartyTraceId(response.getCounterpartyTraceId())
                                                .build();
                        } else {
                                throw new BizException(WechatErrorCode.CHANNEL_ERROR, response.getResultCode(),
                                                response.getResultMsg());
                        }
                } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | InvalidKeySpecException
                                | SignatureException e) {
                        throw new RuntimeException(e);
                }
        }

        @Override
        public WxNativePayResponse nativePay(String outTradeNo, Currency currency, String description,
                        Counterparty counterpartyAccount) {
                PreCreateRequest request;
                request = PreCreateRequest
                                .builder()
                                .goodsDes(description)
                                .orderType(OrderTypeEnum.WECHAT)
                                .txnBeginTs(LocalDateTime.now().format(Constants.QUERY_DATE_TIME_FORMATTER))
                                .amount(currency.getAmountInCent().intValue())
                                .notifyUrl(((FuiouCounterpartyAccountInfo) counterpartyAccount
                                                .getCounterpartyAccount())
                                                .getPreCreateNotifyUrl())
                                .termIp(NetUtils.getHostAddress())
                                .build();

                if (StringUtils.length(outTradeNo) > 30) {
                        request.setOrderId(StringUtils.join(
                                        ((FuiouCounterpartyAccountInfo) counterpartyAccount
                                                        .getCounterpartyAccount())
                                                        .getOrderPrefix(),
                                        this.generateOrderId()));
                }
                request.setTermId(Consts.TERM_ID);
                this.fillInMerchantCodeAndInsCd(counterpartyAccount, request);
                request.generateRandomStr();

                try {
                        this.fillInSignature(counterpartyAccount, request);
                        PreCreateResponse response = this.restClient.post(
                                        UrlBuilder
                                                        .ofHttp(((FuiouCounterpartyAccountInfo) counterpartyAccount
                                                                        .getCounterpartyAccount())
                                                                        .getDomainName())
                                                        .addPath(Consts.PRE_CREATE)
                                                        .build(),
                                        request,
                                        ENTITY_BUILDER,
                                        PreCreateResponse.class,
                                        true,
                                        false);

                        if (response.isSuccess()) {
                                this.verifySign(counterpartyAccount, response);
                                // response.verifySign(this.publicKey);
                                return WxNativePayResponse
                                                .builder()
                                                .counterpartyTxnId(response.getCounterpartyTxnId())
                                                .qrCode(response.getQrCode())
                                                .channelTransactionId(request.getOrderId())
                                                .build();
                        } else {
                                throw new BizException(WechatErrorCode.CHANNEL_ERROR, response.getResultCode(),
                                                response.getResultMsg());
                        }
                } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | InvalidKeySpecException
                                | SignatureException e) {
                        throw new RuntimeException(e);
                }
        }
}
