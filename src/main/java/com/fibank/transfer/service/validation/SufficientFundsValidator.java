package com.fibank.transfer.service.validation;

import com.fibank.transfer.exception.InsufficientFundsException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** Rejects transfers the source account cannot cover. */
@Component
@Order(3)
public class SufficientFundsValidator implements TransferValidator {

    @Override
    public void validate(TransferContext context) {
        if (!context.source().hasSufficientFunds(context.amount())) {
            throw new InsufficientFundsException(context.source().getIban());
        }
    }
}
