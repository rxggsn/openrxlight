package cn.ggsn.openrxlight.transaction.pay;

import java.util.UUID;

import cn.ggsn.openrxlight.model.Currency;
import cn.ggsn.openrxlight.transaction.domain.ChannelType;
import cn.ggsn.openrxlight.transaction.domain.TransactionType;
import cn.ggsn.openrxlight.transaction.pay.domain.Counterparty;
import cn.ggsn.openrxlight.transaction.pay.wechat.domain.WechatPayOrder;
import cn.ggsn.openrxlight.transaction.pay.wechat.domain.WechatPayOrderQuery;
import cn.ggsn.openrxlight.transaction.pay.wechat.domain.WechatRefund;
import cn.ggsn.openrxlight.transaction.pay.wechat.domain.WechatRefundQuery;
import cn.ggsn.openrxlight.transaction.pay.wechat.response.WechatPrepayResponse;
import cn.ggsn.openrxlight.transaction.pay.wechat.response.WxNativePayResponse;
import lombok.NonNull;

public interface WechatPayApi {
        WechatPrepayResponse prepay(String orderId, Currency currency,
                        String description, String openId,
                        @NonNull Counterparty account);

        WechatRefund refund(String outTradeNo,
                        Currency refundCcy,
                        Currency originCcy,
                        UUID outRefundNo,
                        @NonNull Counterparty account);

        WechatPayOrder queryPayOrder(WechatPayOrderQuery query, @NonNull Counterparty counterpartyAccount);

        WechatRefund queryRefund(WechatRefundQuery query);

        boolean supports(TransactionType transactionType, ChannelType channelType);

        WxNativePayResponse nativePay(String outTradeNo, Currency currency, String description,
                        Counterparty counterpartyAccount);
}
