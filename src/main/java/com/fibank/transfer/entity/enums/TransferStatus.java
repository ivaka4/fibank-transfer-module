package com.fibank.transfer.entity.enums;

/**
 * Lifecycle status of a transfer. A transfer is atomic: it either fully
 * completes (balances updated + ledger written) or fails without side effects.
 */
public enum TransferStatus {
    COMPLETED,
    FAILED
}
