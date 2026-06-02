package com.fibank.transfer.service.fx;

import com.fibank.transfer.common.Money;
import com.fibank.transfer.entity.enums.Currency;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Default {@link CurrencyConverter} backed by a configurable property
 * ({@code fx.rate.usd-to-eur}). USD→EUR uses the rate directly; EUR→USD uses its
 * reciprocal; same-currency conversions are a no-op (rate = 1).
 */
@Component
public class PropertyBasedCurrencyConverter implements CurrencyConverter {

    /** Scale used for the reciprocal rate to retain precision on EUR→USD. */
    private static final int RATE_SCALE = 6;

    private final BigDecimal usdToEur;

    public PropertyBasedCurrencyConverter(@Value("${fx.rate.usd-to-eur}") BigDecimal usdToEur) {
        this.usdToEur = usdToEur;
    }

    @Override
    public Money convert(Money source, Currency targetCurrency) {
        if (source.currency() == targetCurrency) {
            return source;
        }
        BigDecimal converted = source.amount().multiply(rate(source.currency(), targetCurrency));
        return Money.of(converted, targetCurrency);
    }

    @Override
    public BigDecimal rate(Currency from, Currency to) {
        if (from == to) {
            return BigDecimal.ONE;
        }
        if (from == Currency.USD && to == Currency.EUR) {
            return usdToEur;
        }
        if (from == Currency.EUR && to == Currency.USD) {
            return BigDecimal.ONE.divide(usdToEur, RATE_SCALE, RoundingMode.HALF_UP);
        }
        throw new IllegalArgumentException("Unsupported currency pair: %s -> %s".formatted(from, to));
    }
}
