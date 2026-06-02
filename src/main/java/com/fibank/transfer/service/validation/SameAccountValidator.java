package com.fibank.transfer.service.validation;

import com.fibank.transfer.exception.InvalidTransferException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** Rejects transfers where the source and destination are the same account. */
@Component
@Order(2)
public class SameAccountValidator implements TransferValidator {

    @Override
    public void validate(TransferContext context) {
        if (context.source().getIban().equals(context.destination().getIban())) {
            throw new InvalidTransferException("Source and destination accounts must be different");
        }
    }
}
