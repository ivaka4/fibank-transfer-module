package com.fibank.transfer.service.model;

import com.fibank.transfer.entity.enums.Currency;

import java.math.BigDecimal;

/** Service-layer input for creating a standing order. */
public record CreateStandingOrderCommand(
        String sourceIban,
        String destinationIban,
        BigDecimal amount,
        Currency currency,
        String cronExpression
) {
}
