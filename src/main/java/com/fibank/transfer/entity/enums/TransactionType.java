package com.fibank.transfer.entity.enums;

/**
 * Direction of a ledger entry in the double-entry bookkeeping model.
 * Every successful transfer produces exactly one {@link #DEBIT} (source)
 * and one {@link #CREDIT} (destination) entry.
 */
public enum TransactionType {
    DEBIT,
    CREDIT
}
