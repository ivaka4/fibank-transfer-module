package com.fibank.transfer.service.validation;

import com.fibank.transfer.exception.InvalidTransferException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** Rejects transfers whose amount is not strictly positive. */
@Component
@Order(1)
public class PositiveAmountValidator implements TransferValidator {

    @Override
    public void validate(TransferContext context) {
        if (context.amount() == null || context.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransferException("Transfer amount must be greater than zero");
        }
    }
}
