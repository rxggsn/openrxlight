package cn.ggsn.openrxlight.services;

import java.util.TreeMap;

import com.google.common.collect.Maps;

import cn.ggsn.openrxlight.config.Config;
import cn.ggsn.openrxlight.model.billing.BillingInfo;
import cn.ggsn.openrxlight.model.billing.CreditPlan;
import cn.ggsn.openrxlight.model.billing.PaymentChannel;
import cn.ggsn.openrxlight.request.billing.PayForAddedOnCreditRequest;
import cn.ggsn.openrxlight.request.billing.UpgradeCreditPlanRequest;
import cn.ggsn.openrxlight.response.PageResult;
import cn.ggsn.openrxlight.response.billing.UpgradeCreditPlanResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BillingService {
    private final Config config;

    public BillingInfo getBillingInfo() throws Exception {
        return cn.ggsn.openrxlight.utils.Transport.send(this.config, null,
                "/billing",
                cn.ggsn.openrxlight.httpx.HttpMethod.GET.getName(), null)
                .getBody(BillingInfo.class, this.config);
    }

    public UpgradeCreditPlanResponse upgradeCreditPlan(UpgradeCreditPlanRequest request) throws Exception {
        return cn.ggsn.openrxlight.utils.Transport.send(this.config, request,
                "/billing/upgrade/credit_plan",
                cn.ggsn.openrxlight.httpx.HttpMethod.POST.getName(), null)
                .getBody(UpgradeCreditPlanResponse.class, this.config);
    }

    public UpgradeCreditPlanResponse payForAddedOnCredit(PayForAddedOnCreditRequest request) throws Exception {
        return cn.ggsn.openrxlight.utils.Transport.send(this.config, request,
                "/billing/upgrade/added_on_credit",
                cn.ggsn.openrxlight.httpx.HttpMethod.POST.getName(), null)
                .getBody(UpgradeCreditPlanResponse.class, this.config);
    }

    public PageResult<CreditPlan> listAvailableCreditPlans(String currencyType, PaymentChannel paymentChannel)
            throws Exception {
        TreeMap<String, String> params = Maps.newTreeMap();
        params.put("currency_type", currencyType);
        params.put("payment_channel", paymentChannel.name().toLowerCase());
        return cn.ggsn.openrxlight.utils.Transport.send(this.config, null,
                "/billing/credit_plans",
                cn.ggsn.openrxlight.httpx.HttpMethod.GET.getName(), params)
                .getPageResult(CreditPlan.class, this.config);
    }
}
