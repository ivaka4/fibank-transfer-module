package com.fibank.transfer.service.validation;

/**
 * One link in the transfer validation chain (Chain of Responsibility). Each
 * implementation checks a single rule and throws a {@code BusinessException} on
 * violation. Order is controlled with {@code @Order}; a new rule is added simply by
 * introducing a new {@code @Component} — the existing links stay untouched (OCP).
 */
public interface TransferValidator {

    void validate(TransferContext context);
}
