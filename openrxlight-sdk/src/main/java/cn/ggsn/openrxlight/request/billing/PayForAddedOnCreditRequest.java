package cn.ggsn.openrxlight.request.billing;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import cn.ggsn.openrxlight.model.billing.PaymentChannel;
import cn.ggsn.openrxlight.request.Required;
import cn.ggsn.openrxlight.request.Validate;
import cn.ggsn.openrxlight.utils.CurrencyTypeChecker;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PayForAddedOnCreditRequest implements Validate {

    @Required
    private Integer creditCount;
    @Required
    private PaymentChannel paymentChannel;
    @Required
    private String currencyType;

    @Override
    public void validate() {
        Validate.super.validate();
        if (creditCount < 1) {
            throw new IllegalArgumentException("count must be at least 1");
        }

        if (!CurrencyTypeChecker.checkCurrencyType(currencyType)) {
            throw new IllegalArgumentException("unsupported currency type: " + currencyType);
        }
    }

}
