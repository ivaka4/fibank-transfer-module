package com.fibank.transfer.exception;

import java.util.UUID;

/** Raised when a transfer with the given id does not exist. Maps to HTTP 404. */
public class TransferNotFoundException extends BusinessException {

    public TransferNotFoundException(UUID id) {
        super("Transfer not found: " + id);
    }
}
