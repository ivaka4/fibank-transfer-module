package com.fibank.transfer.exception;

/**
 * Base type for all domain/business rule violations. Kept free of any web
 * (HTTP) concern — the mapping from exception type to HTTP status lives in the
 * {@code GlobalExceptionHandler}, preserving separation of concerns.
 */
public abstract class BusinessException extends RuntimeException {

    protected BusinessException(String message) {
        super(message);
    }
}
