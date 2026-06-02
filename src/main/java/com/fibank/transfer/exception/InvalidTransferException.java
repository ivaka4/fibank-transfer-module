package com.fibank.transfer.exception;

/**
 * Raised for transfer requests that violate a business rule unrelated to funds,
 * e.g. non-positive amount or identical source and destination accounts. Maps to HTTP 422.
 */
public class InvalidTransferException extends BusinessException {

    public InvalidTransferException(String message) {
        super(message);
    }
}
