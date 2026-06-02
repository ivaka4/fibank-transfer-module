package com.fibank.transfer.service;

import com.fibank.transfer.service.model.TransferCommand;
import com.fibank.transfer.service.model.TransferResult;

import java.util.UUID;

public interface TransferService {

    /**
     * Executes a one-time transfer atomically and idempotently: validates the request,
     * applies any FX conversion, moves the funds with double-entry bookkeeping, and
     * records the outcome. A replayed idempotency key returns the original result.
     */
    TransferResult execute(TransferCommand command);

    /** Returns a previously executed transfer, or throws if it does not exist. */
    TransferResult getById(UUID id);
}
