package cn.ggsn.openrxlight.transaction.pay.wechat;

import cn.ggsn.openrxlight.lang.Lists2;
import cn.ggsn.openrxlight.lang.Maps2;
import cn.ggsn.openrxlight.model.Currency;
import cn.ggsn.openrxlight.transaction.domain.ChannelType;
import cn.ggsn.openrxlight.transaction.domain.PaymentCounterpartyAccount;
import cn.ggsn.openrxlight.transaction.domain.TransactionType;
import cn.ggsn.openrxlight.utils.UUIdConverter;
import cn.ggsn.openrxlight.transaction.pay.WechatPayApi;
import cn.ggsn.openrxlight.transaction.pay.domain.Counterparty;
import cn.ggsn.openrxlight.transaction.pay.wechat.domain.WechatPayAccountInfo;
import cn.ggsn.openrxlight.transaction.pay.wechat.domain.WechatPayOrder;
import cn.ggsn.openrxlight.transaction.pay.wechat.domain.WechatPayOrderQuery;
import cn.ggsn.openrxlight.transaction.pay.wechat.domain.WechatRefund;
import cn.ggsn.openrxlight.transaction.pay.wechat.domain.WechatRefundQuery;
import cn.ggsn.openrxlight.transaction.pay.wechat.response.WechatPrepayResponse;
import cn.ggsn.openrxlight.transaction.pay.wechat.response.WxNativePayResponse;

import com.github.binarywang.wxpay.bean.request.WxPayRefundV3Request;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderV3Request;
import com.github.binarywang.wxpay.bean.result.WxPayOrderQueryV3Result;
import com.github.binarywang.wxpay.bean.result.WxPayRefundV3Result;
import com.github.binarywang.wxpay.bean.result.WxPayUnifiedOrderV3Result;
import com.github.binarywang.wxpay.bean.result.enums.TradeTypeEnum;
import com.github.binarywang.wxpay.constant.WxPayConstants;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.UUID;

// @Singleton
@Slf4j
class WechatPayClient implements WechatPayApi {
    private final List<TransactionType> supportTransactionTypes;
    @Getter
    private final Map<UUID, WechatPayConfig> wechatPayConfigMap = Maps2.empty();
    private final CounterpartyWeChatPayConfigContainer container = new CounterpartyWeChatPayConfigContainer();

    @SuppressWarnings("null")
    WechatPayClient() {
        List<PaymentCounterpartyAccount> accounts = PaymentCounterpartyAccount
                .listByChannelType(ChannelType.WECHAT_OFFICIAL_PAY);
        this.supportTransactionTypes = Lists2.flatMapNotNull(accounts, PaymentCounterpartyAccount::getTransactionTypes)
                .stream()
                .map(TransactionType::fromValue)
                .distinct()
                .toList();
        Lists2.foreach(accounts, account -> {
            WechatPayConfig config = new WechatPayConfig();
            WechatPayAccountInfo additionalInfo = (WechatPayAccountInfo) account
                    .getAdditionalInfo();
            config.setMchId(additionalInfo.getMchId());
            config.setApiV3Key(additionalInfo.getApiV3Key());
            config.setNotifyUrl(((WechatPayAccountInfo) account.getAdditionalInfo()).getNotifyUrl());
            config.setPrivateKey(additionalInfo.getPrivateKey());
            config.setMerchantSerialNumber(additionalInfo.getMerchantSerialNumber());
            config.setPrivateCertPath(additionalInfo.getPrivateCertPath());

            this.wechatPayConfigMap.put(account.getAdditionalInfo().getAccountId(), config);
            this.container.addConfig(account.getAdditionalInfo().getAccountId(), config);
        });
    }

    @Override
    public WechatPrepayResponse prepay(
            String transactionId,
            Currency currency,
            String description,
            String openId,
            @NonNull Counterparty account) {
        WxPayUnifiedOrderV3Request v3Request = new WxPayUnifiedOrderV3Request();
        WxPayUnifiedOrderV3Request.Amount amount = new WxPayUnifiedOrderV3Request.Amount();
        var wechatAccount = (WechatPayAccountInfo) account.getCounterpartyAccount();
        amount.setCurrency(currency.getCurrencyType());
        amount.setTotal(currency.getAmount().multiply(cn.ggsn.openrxlight.Constants.FEE_UNIT).intValue());
        v3Request.setAmount(amount);
        v3Request.setAppid(wechatAccount.getAppId());
        v3Request.setDescription(description);
        v3Request.setOutTradeNo(transactionId);
        v3Request.setPayer(new WxPayUnifiedOrderV3Request.Payer().setOpenid(openId));
        v3Request.setNotifyUrl(((WechatPayAccountInfo) account.getCounterpartyAccount()).getNotifyUrl());

        try {
            WxPayUnifiedOrderV3Result.JsapiResult v3Result = this
                    .getOrCreateWxPayService(account)
                    .createOrderV3(TradeTypeEnum.JSAPI, v3Request);
            return WechatPrepayResponse.builder()
                    .appId(v3Result.getAppId())
                    .nonceStr(v3Result.getNonceStr())
                    .packageValue(v3Result.getPackageValue())
                    .paySign(v3Result.getPaySign())
                    .signType(v3Result.getSignType())
                    .timestamp(v3Result.getTimeStamp())
                    .build();
        } catch (WxPayException e) {
            throw new RuntimeException(e);
        }
    }

    private WxPayService getOrCreateWxPayService(@NonNull Counterparty account) {
        // dynamically create WxPayService for counterparty account
        if (this.container.containsWxService(account.getCounterpartyId())) {
            return this.container.getWxService(account.getCounterpartyId());
        }
        WechatPayConfig config = new WechatPayConfig();
        WechatPayAccountInfo additionalInfo = (WechatPayAccountInfo) account
                .getCounterpartyAccount();
        config.setMchId(additionalInfo.getMchId());
        config.setApiV3Key(additionalInfo.getApiV3Key());
        config.setNotifyUrl(additionalInfo.getNotifyUrl());
        config.setPrivateKey(additionalInfo.getPrivateKey());
        config.setMerchantSerialNumber(additionalInfo.getMerchantSerialNumber());
        config.setPrivateCertPath(additionalInfo.getPrivateCertPath());

        WxPayService wxPayService = config.createWxPayService();
        this.container.addConfig(account.getCounterpartyId(), config);
        this.container.addWxPayService(account.getCounterpartyId(), wxPayService);
        return wxPayService;
    }

    @Override
    public WechatPayOrder queryPayOrder(WechatPayOrderQuery query, Counterparty counterpartyAccount) {
        try {

            WxPayOrderQueryV3Result queryOrderV3 = this.getOrCreateWxPayService(counterpartyAccount)
                    .queryOrderV3(null, query.getTransactionId());
            return WechatPayOrder.from(queryOrderV3);
        } catch (WxPayException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public WechatRefund refund(String outTradeNo,
            Currency refundCcy,
            Currency originCcy,
            UUID outRefundNo,
            @NonNull Counterparty account) {
        WxPayRefundV3Request request = new WxPayRefundV3Request();
        WxPayRefundV3Request.Amount amount = new WxPayRefundV3Request.Amount();
        amount.setCurrency(refundCcy.getCurrencyType());
        amount.setRefund(refundCcy.getAmount().multiply(cn.ggsn.openrxlight.Constants.FEE_UNIT).intValue());
        amount.setTotal(originCcy.getAmount().multiply(cn.ggsn.openrxlight.Constants.FEE_UNIT).intValue());
        request.setAmount(amount);
        request.setNotifyUrl(((WechatPayAccountInfo) account.getCounterpartyAccount()).getNotifyUrl());
        request.setOutTradeNo(outTradeNo);
        request.setOutRefundNo(UUIdConverter.replaceHyphenWithEmptyChar(outRefundNo));
        try {
            WxPayRefundV3Result v3Result = this.getOrCreateWxPayService(account).refundV3(request);
            return WechatRefund.builder()
                    .rxDomainTxnId(outTradeNo)
                    .refundId(v3Result.getRefundId())
                    .systemRefundId(outRefundNo)
                    .wechatTxnId(v3Result.getTransactionId())
                    .status(WxPayConstants.RefundStatus.PROCESSING)
                    .build();
        } catch (WxPayException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public WechatRefund queryRefund(WechatRefundQuery query) {
        return null;
    }

    @Override
    public boolean supports(TransactionType transactionType, ChannelType channelType) {
        return ChannelType.WECHAT_OFFICIAL_PAY.equals(channelType) &&
                Lists2.contains(this.supportTransactionTypes, txnType -> txnType.equals(transactionType));
    }

    @Override
    public WxNativePayResponse nativePay(String outTradeNo, Currency currency, String description,
            Counterparty counterpartyAccount) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'nativePay'");
    }
}
