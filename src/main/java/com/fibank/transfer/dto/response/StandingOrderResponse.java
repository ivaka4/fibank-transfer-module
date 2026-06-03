package com.fibank.transfer.dto.response;

import com.fibank.transfer.entity.enums.Currency;
import com.fibank.transfer.entity.enums.StandingOrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record StandingOrderResponse(
        UUID id,
        String sourceIban,
        String destinationIban,
        BigDecimal amount,
        Currency currency,
        String cronExpression,
        boolean active,
        Instant createdAt,
        Instant lastRunAt,
        StandingOrderStatus lastRunStatus
) {
}
