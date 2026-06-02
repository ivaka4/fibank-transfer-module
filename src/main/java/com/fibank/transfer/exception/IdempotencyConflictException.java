package com.fibank.transfer.exception;

/**
 * Raised when an idempotency key is reused with a different request body than the
 * original. Reusing a key must be safe only for identical requests; a mismatch is
 * a client error. Maps to HTTP 409 Conflict.
 */
public class IdempotencyConflictException extends BusinessException {

    public IdempotencyConflictException(String key) {
        super("Idempotency key reused with a different request payload: " + key);
    }
}
