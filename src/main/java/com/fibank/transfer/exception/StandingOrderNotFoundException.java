package com.fibank.transfer.exception;

import java.util.UUID;

/** Raised when a standing order with the given id does not exist (or is already cancelled). Maps to HTTP 404. */
public class StandingOrderNotFoundException extends BusinessException {

    public StandingOrderNotFoundException(UUID id) {
        super("Standing order not found: " + id);
    }
}
