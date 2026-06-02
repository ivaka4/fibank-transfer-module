package com.fibank.transfer.service.fx;

import com.fibank.transfer.common.Money;
import com.fibank.transfer.entity.enums.Currency;

import java.math.BigDecimal;

/**
 * Strategy for converting money between currencies.
 *
 * <p>Defined as an interface so the exchange-rate source is a pluggable concern:
 * the default {@link PropertyBasedCurrencyConverter} reads a configured rate, but a
 * live-rate provider (external FX API with caching/fallback) could be dropped in
 * without touching the transfer logic — Open/Closed in action.
 */
public interface CurrencyConverter {

    /** Converts {@code source} into {@code targetCurrency}, applying the current rate. */
    Money convert(Money source, Currency targetCurrency);

    /** The multiplicative rate to go from {@code from} into {@code to} (1 for same currency). */
    BigDecimal rate(Currency from, Currency to);
}
