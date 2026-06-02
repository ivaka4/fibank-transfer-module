package com.fibank.transfer.service.model;

import com.fibank.transfer.entity.TransferEntity;
import com.fibank.transfer.entity.enums.Currency;
import com.fibank.transfer.entity.enums.TransferStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Service-layer result of a transfer. Serialised into the idempotency store so a
 * replayed request can return the original outcome without re-executing.
 */
public record TransferResult(
        UUID id,
        String sourceIban,
        String destinationIban,
        BigDecimal sourceAmount,
        BigDecimal destinationAmount,
        Currency sourceCurrency,
        Currency destinationCurrency,
        BigDecimal fxRate,
        TransferStatus status,
        Instant createdAt
) {

    public static TransferResult from(TransferEntity t) {
        return new TransferResult(
                t.getId(),
                t.getSourceIban(),
                t.getDestinationIban(),
                t.getSourceAmount(),
                t.getDestinationAmount(),
                t.getSourceCurrency(),
                t.getDestinationCurrency(),
                t.getFxRate(),
                t.getStatus(),
                t.getCreatedAt());
    }
}
