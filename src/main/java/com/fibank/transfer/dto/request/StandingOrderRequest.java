package com.fibank.transfer.dto.request;

import com.fibank.transfer.entity.enums.Currency;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** Request body for creating a standing order. */
public record StandingOrderRequest(

        @NotBlank(message = "sourceIban is required")
        String sourceIban,

        @NotBlank(message = "destinationIban is required")
        String destinationIban,

        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.01", message = "amount must be greater than zero")
        BigDecimal amount,

        @NotNull(message = "currency is required")
        Currency currency,

        @NotBlank(message = "cronExpression is required")
        String cronExpression
) {
}
