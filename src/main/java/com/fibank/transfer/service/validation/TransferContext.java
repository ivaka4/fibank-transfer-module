package com.fibank.transfer.service.validation;

import com.fibank.transfer.entity.AccountEntity;

import java.math.BigDecimal;

/**
 * Immutable input to the transfer validation chain: the already-loaded (and locked)
 * source and destination accounts plus the requested amount, expressed in the
 * source account's currency.
 */
public record TransferContext(
        AccountEntity source,
        AccountEntity destination,
        BigDecimal amount
) {
}
