package cn.ggsn.openrxlight.utils;

import java.util.Currency;
import java.util.Set;

public class CurrencyTypeChecker {

    private static final Set<String> ISO_4217_CURRENCIES = Currency.getAvailableCurrencies()
            .stream()
            .map(Currency::getCurrencyCode)
            .collect(java.util.stream.Collectors.toUnmodifiableSet());

    public static boolean checkCurrencyType(String currencyType) {
        if (currencyType == null || currencyType.length() != 3) {
            return false;
        }
        return ISO_4217_CURRENCIES.contains(currencyType);
    }
}
