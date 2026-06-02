package com.fibank.transfer.service.model;

import java.math.BigDecimal;

/**
 * Service-layer input for executing a transfer, decoupled from the web DTO.
 * Carries the client's idempotency key so the service can guarantee
 * exactly-once processing.
 */
public record TransferCommand(
        String sourceIban,
        String destinationIban,
        BigDecimal amount,
        String idempotencyKey
) {

    /**
     * A canonical fingerprint of the request's business parameters. Used to detect
     * an idempotency key being reused with a different payload.
     */
    public String fingerprint() {
        return "%s|%s|%s".formatted(sourceIban, destinationIban,
                amount == null ? "null" : amount.stripTrailingZeros().toPlainString());
    }
}
