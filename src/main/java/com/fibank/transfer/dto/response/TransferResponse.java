package com.fibank.transfer.dto.response;

import com.fibank.transfer.entity.enums.Currency;
import com.fibank.transfer.entity.enums.TransferStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransferResponse(
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
}
