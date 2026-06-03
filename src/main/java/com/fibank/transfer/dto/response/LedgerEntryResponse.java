package com.fibank.transfer.dto.response;

import com.fibank.transfer.entity.enums.Currency;
import com.fibank.transfer.entity.enums.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LedgerEntryResponse(
        Long id,
        String accountIban,
        UUID transferId,
        TransactionType type,
        BigDecimal amount,
        Currency currency,
        String correlationId,
        Instant createdAt
) {
}
