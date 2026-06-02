package com.fibank.transfer.service.validation;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Runs the ordered chain of {@link TransferValidator}s. Spring injects all validators
 * sorted by their {@code @Order}, so the chain composition is declarative and
 * extensible — the orchestrator never changes when a rule is added or removed.
 */
@Component
public class TransferValidationChain {

    private final List<TransferValidator> validators;

    public TransferValidationChain(List<TransferValidator> validators) {
        this.validators = validators;
    }

    /** Executes every validator in order; the first violation throws and stops the chain. */
    public void validate(TransferContext context) {
        validators.forEach(validator -> validator.validate(context));
    }
}
