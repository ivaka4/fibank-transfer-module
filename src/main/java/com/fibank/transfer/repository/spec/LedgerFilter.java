package com.fibank.transfer.repository.spec;

import com.fibank.transfer.entity.enums.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Immutable carrier of the optional ledger query filters. Any {@code null} field
 * means "no constraint on this dimension"; present fields are combined with AND.
 */
public record LedgerFilter(
        String accountIban,
        Instant dateFrom,
        Instant dateTo,
        TransactionType type,
        BigDecimal minAmount,
        BigDecimal maxAmount
) {
}
