package com.fibank.transfer.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Request body for executing a one-time transfer. The amount is expressed in the
 * source account's currency. Bean Validation enforces structural correctness;
 * business rules are checked by the service's validation chain.
 */
public record TransferRequest(

        @NotBlank(message = "sourceIban is required")
        String sourceIban,

        @NotBlank(message = "destinationIban is required")
        String destinationIban,

        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.01", message = "amount must be greater than zero")
        BigDecimal amount
) {
}
