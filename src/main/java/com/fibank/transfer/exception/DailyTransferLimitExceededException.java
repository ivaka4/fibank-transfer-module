package com.fibank.transfer.exception;

import java.math.BigDecimal;

/** Raised when an account's cumulative outgoing transfers for the day would exceed the daily limit. Maps to HTTP 422. */
public class DailyTransferLimitExceededException extends BusinessException {

    public DailyTransferLimitExceededException(String iban, BigDecimal limit) {
        super("Daily transfer limit of %s exceeded for account %s".formatted(limit.toPlainString(), iban));
    }
}
