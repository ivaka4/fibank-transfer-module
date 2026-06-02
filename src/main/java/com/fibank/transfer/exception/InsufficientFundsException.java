package com.fibank.transfer.exception;

/** Raised when the source account does not hold enough funds for a transfer. Maps to HTTP 422. */
public class InsufficientFundsException extends BusinessException {

    public InsufficientFundsException(String iban) {
        super("Insufficient funds in source account " + iban);
    }
}
