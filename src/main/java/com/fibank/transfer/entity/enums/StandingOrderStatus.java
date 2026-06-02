package com.fibank.transfer.entity.enums;

/**
 * Outcome of the most recent execution of a standing order. Stored so that
 * a failed run is visible and auditable; a failed order stays active and is
 * retried on the next scheduled run (never skipped silently).
 */
public enum StandingOrderStatus {
    PENDING,
    SUCCESS,
    FAILED
}
