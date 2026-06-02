package com.fibank.transfer.service;

import com.fibank.transfer.service.model.TransferResult;

import java.util.Optional;

/**
 * Guards transfer execution against duplicate processing keyed by the client's
 * {@code X-Idempotency-Key}. Stored outcomes expire after a configured TTL.
 */
public interface IdempotencyService {

    /**
     * Returns the previously stored result for {@code key} if one exists and has not
     * expired. Throws {@code IdempotencyConflictException} if the key was used before
     * with a different request fingerprint.
     */
    Optional<TransferResult> findExisting(String key, String fingerprint);

    /** Persists the outcome of a freshly executed request under {@code key}. */
    void store(String key, String fingerprint, TransferResult result);
}
