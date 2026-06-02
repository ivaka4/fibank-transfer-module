package com.fibank.transfer.service.impl;

import com.fibank.transfer.common.CorrelationId;
import com.fibank.transfer.common.Money;
import com.fibank.transfer.entity.AccountEntity;
import com.fibank.transfer.entity.LedgerEntryEntity;
import com.fibank.transfer.entity.TransferEntity;
import com.fibank.transfer.entity.enums.TransferStatus;
import com.fibank.transfer.exception.AccountNotFoundException;
import com.fibank.transfer.exception.TransferNotFoundException;
import com.fibank.transfer.repository.AccountRepository;
import com.fibank.transfer.repository.LedgerEntryRepository;
import com.fibank.transfer.repository.TransferRepository;
import com.fibank.transfer.service.IdempotencyService;
import com.fibank.transfer.service.TransferService;
import com.fibank.transfer.service.factory.LedgerEntryFactory;
import com.fibank.transfer.service.fx.CurrencyConverter;
import com.fibank.transfer.service.model.TransferCommand;
import com.fibank.transfer.service.model.TransferResult;
import com.fibank.transfer.service.validation.TransferContext;
import com.fibank.transfer.service.validation.TransferValidationChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Core transfer orchestration. The whole {@link #execute} method runs in a single
 * transaction so that balance updates, the transfer record and the double-entry
 * ledger commit atomically — or roll back together on any failure.
 *
 * <p>Concurrency: source and destination accounts are loaded with a pessimistic
 * write-lock acquired in a deterministic IBAN order, which serialises competing
 * transfers on the same account and makes deadlocks impossible.
 */
@Service
public class TransferServiceImpl implements TransferService {

    private static final Logger log = LoggerFactory.getLogger(TransferServiceImpl.class);

    private final AccountRepository accountRepository;
    private final TransferRepository transferRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final CurrencyConverter currencyConverter;
    private final TransferValidationChain validationChain;
    private final LedgerEntryFactory ledgerEntryFactory;
    private final IdempotencyService idempotencyService;

    public TransferServiceImpl(AccountRepository accountRepository,
                               TransferRepository transferRepository,
                               LedgerEntryRepository ledgerEntryRepository,
                               CurrencyConverter currencyConverter,
                               TransferValidationChain validationChain,
                               LedgerEntryFactory ledgerEntryFactory,
                               IdempotencyService idempotencyService) {
        this.accountRepository = accountRepository;
        this.transferRepository = transferRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.currencyConverter = currencyConverter;
        this.validationChain = validationChain;
        this.ledgerEntryFactory = ledgerEntryFactory;
        this.idempotencyService = idempotencyService;
    }

    @Override
    @Transactional
    public TransferResult execute(TransferCommand command) {
        // 1. Idempotency: a replayed key returns the original outcome unchanged.
        Optional<TransferResult> existing =
                idempotencyService.findExisting(command.idempotencyKey(), command.fingerprint());
        if (existing.isPresent()) {
            log.info("Idempotent replay for key={}, returning stored transfer {}",
                    command.idempotencyKey(), existing.get().id());
            return existing.get();
        }

        log.info("Transfer attempt: {} -> {}, amount={}",
                command.sourceIban(), command.destinationIban(), command.amount());

        try {
            TransferResult result = doExecute(command);
            idempotencyService.store(command.idempotencyKey(), command.fingerprint(), result);
            log.info("Transfer {} completed: {} {} -> {} {}",
                    result.id(), result.sourceAmount(), result.sourceCurrency(),
                    result.destinationAmount(), result.destinationCurrency());
            return result;
        } catch (RuntimeException e) {
            log.warn("Transfer failed: {} -> {}, amount={}: {}",
                    command.sourceIban(), command.destinationIban(), command.amount(), e.getMessage());
            throw e;
        }
    }

    private TransferResult doExecute(TransferCommand command) {
        String correlationId = MDC.get(CorrelationId.MDC_KEY);

        // 2. Load both accounts with a pessimistic lock in deterministic IBAN order.
        Map<String, AccountEntity> locked =
                lockAccountsInOrder(command.sourceIban(), command.destinationIban());
        AccountEntity source = locked.get(command.sourceIban());
        AccountEntity destination = locked.get(command.destinationIban());

        // 3. Business validation chain (positive amount, distinct accounts, funds, daily limit).
        validationChain.validate(new TransferContext(source, destination, command.amount()));

        // 4. FX conversion: debit in source currency, credit in destination currency.
        Money sourceMoney = Money.of(command.amount(), source.getCurrency());
        Money destinationMoney = currencyConverter.convert(sourceMoney, destination.getCurrency());
        BigDecimal rate = currencyConverter.rate(source.getCurrency(), destination.getCurrency());

        // 5. Move the funds (balance invariants enforced inside the entities).
        source.debit(sourceMoney.amount());
        destination.credit(destinationMoney.amount());

        // 6. Persist the transfer record. saveAndFlush forces the INSERT now so the
        //    @CreationTimestamp is populated and reflected in the returned result.
        TransferEntity transfer = TransferEntity.builder()
                .sourceIban(source.getIban())
                .destinationIban(destination.getIban())
                .sourceAmount(sourceMoney.amount())
                .destinationAmount(destinationMoney.amount())
                .sourceCurrency(source.getCurrency())
                .destinationCurrency(destination.getCurrency())
                .fxRate(rate)
                .status(TransferStatus.COMPLETED)
                .idempotencyKey(command.idempotencyKey())
                .correlationId(correlationId)
                .build();
        transferRepository.saveAndFlush(transfer);

        // 7. Append the double-entry ledger pair (DEBIT source + CREDIT destination).
        List<LedgerEntryEntity> entries = ledgerEntryFactory.createDoubleEntry(transfer, correlationId);
        ledgerEntryRepository.saveAll(entries);

        return TransferResult.from(transfer);
    }

    /**
     * Locks the involved accounts in ascending IBAN order to guarantee a global lock
     * ordering across all transfers, eliminating deadlocks. Returns a map keyed by IBAN.
     */
    private Map<String, AccountEntity> lockAccountsInOrder(String sourceIban, String destinationIban) {
        Map<String, AccountEntity> locked = new LinkedHashMap<>();
        List<String> ordered = List.of(sourceIban, destinationIban).stream()
                .distinct()
                .sorted()
                .toList();
        for (String iban : ordered) {
            AccountEntity account = accountRepository.findByIbanForUpdate(iban)
                    .orElseThrow(() -> new AccountNotFoundException(iban));
            locked.put(iban, account);
        }
        return locked;
    }

    @Override
    @Transactional(readOnly = true)
    public TransferResult getById(UUID id) {
        return transferRepository.findById(id)
                .map(TransferResult::from)
                .orElseThrow(() -> new TransferNotFoundException(id));
    }
}
