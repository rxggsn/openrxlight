package cn.ggsn.openrxlight.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import cn.ggsn.openrxlight.Constants;
import cn.ggsn.openrxlight.request.Required;
import cn.ggsn.openrxlight.request.Validate;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@NoArgsConstructor
@EqualsAndHashCode
@JsonNaming(com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Currency implements Validate, Comparable<Currency>, Cloneable {
    public static final Currency ZERO = new Currency(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
            "CNY");
    @Required
    private BigDecimal amount;
    @Required
    private String currencyType;

    public Currency(BigDecimal amount, String currencyType) {
        this.amount = amount;
        this.currencyType = currencyType;
        this.resetScale();
    }

    public static Currency zero() {
        return new Currency(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), "CNY");
    }

    public static Currency ofCent(String currencyType, int amount) {
        return new Currency(new BigDecimal(amount).divide(Constants.FEE_UNIT, 2, RoundingMode.HALF_UP),
                currencyType);
    }

    public static Currency ofCent(String currencyType, Long amount) {
        return new Currency(new BigDecimal(amount).divide(Constants.FEE_UNIT, 2, RoundingMode.HALF_UP),
                currencyType);
    }

    /**
     * sum currency a and b
     *
     * @param a: currency a
     * @param b: currency b
     * @return sum of a and b
     */
    public static Currency sum(Currency a, Currency b) {
        return new Currency(a.amount.add(b.amount), a.getCurrencyType());
    }

    public void subtract(Currency currency) {
        this.amount = this.amount.subtract(currency.amount);
    }

    public void plus(Currency currency) {
        this.amount = this.amount.add(currency.amount);
    }

    @Override
    public int compareTo(Currency o) {
        return this.amount.compareTo(o.amount);
    }

    @Override
    public Currency clone() {
        try {
            Currency clone = (Currency) super.clone();
            return new Currency(clone.amount, clone.currencyType);
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public void resetScale() {
        this.amount = this.amount.setScale(2, RoundingMode.HALF_UP);
    }

    @JsonIgnore
    public BigDecimal getAmountInCent() {
        return this.amount.multiply(Constants.FEE_UNIT)
                .setScale(0, RoundingMode.HALF_UP);
    }

    public Currency transferTo(String currencyType) {
        // TODO: will implement later
        return this;
    }

    public Currency multiply(BigDecimal value) {
        return new Currency(this.amount.multiply(value), this.currencyType);
    }

    public Currency subtractAndReturn(Currency feeCcy) {
        return new Currency(this.amount.subtract(feeCcy.amount), this.currencyType);
    }

    public Currency plusAndReturn(Currency feeCcy) {
        return new Currency(this.amount.add(feeCcy.amount), this.currencyType);
    }
}
