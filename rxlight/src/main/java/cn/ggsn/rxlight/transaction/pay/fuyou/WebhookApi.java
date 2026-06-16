package cn.ggsn.rxlight.transaction.pay.fuyou;

import java.net.URLDecoder;

import org.apache.commons.lang.StringUtils;

import cn.ggsn.openrxlight.errorx.BizException;
import cn.ggsn.openrxlight.transaction.domain.Transaction;
import cn.ggsn.openrxlight.transaction.domain.TransactionStatus;
import cn.ggsn.openrxlight.transaction.error.TpsErrorCode;
import cn.ggsn.openrxlight.utils.JsonUtils;
import cn.ggsn.openrxlight.utils.XmlUtils;
import cn.ggsn.rxlight.transaction.pay.fuyou.response.PrepayResultNotify;
import cn.ggsn.rxlight.transaction.vo.CallbackAction;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import lombok.extern.slf4j.Slf4j;

@Path("/transactions/webhooks/fuyou")
@Slf4j
public class WebhookApi {

    @POST
    @Path("/{callbackAction}")
    public void handleWebhook(@PathParam("callbackAction") CallbackAction callbackAction, String requestBody)
            throws Exception {
        String decoded = URLDecoder.decode(
                URLDecoder.decode(StringUtils.removeStart(requestBody, "req="), Consts.CHARSET),
                Consts.CHARSET);
        switch (callbackAction) {
            case prepay:
            case pre_create:
                PrepayResultNotify notify = XmlUtils.fromXml(decoded, PrepayResultNotify.class);
                log.info("fuyou notification body [{}]", JsonUtils.toJson(notify));
                if (notify != null && notify.isSuccess()) {
                    // notify.verifySign(this.fuyouConfig.getPublicKey());
                    var transaction = Transaction.findByChannelTransactionId(notify.getTransactionId())
                            .orElseThrow(() -> new BizException(TpsErrorCode.TransactionNotFound,
                                    notify.getTransactionId()));

                    if (!transaction.isComplete()) {
                        processSuccessPayTransaction(transaction, TransactionStatus.SUCCESS,
                                notify.getCounterpartyTxnId());

                        transaction.getTransactionInfo()
                                .getCounterpartyTransactionInfo()
                                .tryUpdateCounterpartyTraceId(notify.getCounterpartyTraceId());
                        transaction.updateTransactionInfo(transaction.getTransactionInfo());
                    }
                }
                break;
            default:
                break;
        }
    }

    private void processSuccessPayTransaction(Transaction transaction,
            TransactionStatus status, String counterpartyTxnId) {
        if (transaction.isComplete()) {
            log.trace("transaction already completed");
            return;
        }
        // if
        // (TransactionType.WALLET_DEPOSIT.equals(transaction.checkTransactionType())) {
        // this.walletSvc.deposit(WalletDepositRequest.builder()
        // .currency(transaction.getCcy())
        // .walletId(transaction.getTransactionInfo()
        // .getDestination()
        // .getCounterpartyId())
        // .build());
        // }
        transaction.updateCounterpartyTxnIdAndStatusById(status, counterpartyTxnId,
                transaction.getChannelTransactionId());
    }
}
