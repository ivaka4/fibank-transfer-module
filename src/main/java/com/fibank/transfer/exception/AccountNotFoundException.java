package com.fibank.transfer.exception;

/** Raised when a referenced account IBAN does not exist. Maps to HTTP 404. */
public class AccountNotFoundException extends BusinessException {

    public AccountNotFoundException(String iban) {
        super("Account not found: " + iban);
    }
}
