package com.fibank.transfer.service;

import com.fibank.transfer.common.Money;
import com.fibank.transfer.entity.enums.Currency;
import com.fibank.transfer.service.fx.PropertyBasedCurrencyConverter;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CurrencyConverterTest {

    private final PropertyBasedCurrencyConverter converter =
            new PropertyBasedCurrencyConverter(new BigDecimal("0.86"));

    @Test
    void convertsUsdToEurUsingConfiguredRate() {
        Money result = converter.convert(Money.of(new BigDecimal("1000.00"), Currency.USD), Currency.EUR);

        assertThat(result.currency()).isEqualTo(Currency.EUR);
        assertThat(result.amount()).isEqualByComparingTo("860.00");
    }

    @Test
    void convertsEurToUsdUsingReciprocalRate() {
        Money result = converter.convert(Money.of(new BigDecimal("860.00"), Currency.EUR), Currency.USD);

        // 860 / 0.86 = 1000
        assertThat(result.currency()).isEqualTo(Currency.USD);
        assertThat(result.amount()).isEqualByComparingTo("1000.00");
    }

    @Test
    void sameCurrencyIsNoOpWithRateOne() {
        assertThat(converter.rate(Currency.USD, Currency.USD)).isEqualByComparingTo("1");
        Money result = converter.convert(Money.of(new BigDecimal("500.00"), Currency.USD), Currency.USD);
        assertThat(result.amount()).isEqualByComparingTo("500.00");
    }
}
