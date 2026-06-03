package com.fibank.transfer.dto.response;

import com.fibank.transfer.entity.enums.Currency;

import java.math.BigDecimal;

public record AccountResponse(
        String iban,
        String ownerName,
        Currency currency,
        BigDecimal balance
) {
}
