package com.fibank.transfer.service;

import com.fibank.transfer.entity.AccountEntity;
import com.fibank.transfer.entity.TransferEntity;
import com.fibank.transfer.entity.enums.Currency;
import com.fibank.transfer.exception.DailyTransferLimitExceededException;
import com.fibank.transfer.exception.InsufficientFundsException;
import com.fibank.transfer.repository.AccountRepository;
import com.fibank.transfer.repository.LedgerEntryRepository;
import com.fibank.transfer.repository.TransferRepository;
import com.fibank.transfer.service.factory.LedgerEntryFactory;
import com.fibank.transfer.service.fx.CurrencyConverter;
import com.fibank.transfer.service.fx.PropertyBasedCurrencyConverter;
import com.fibank.transfer.service.impl.TransferServiceImpl;
import com.fibank.transfer.service.model.TransferCommand;
import com.fibank.transfer.service.model.TransferResult;
import com.fibank.transfer.service.validation.TransferContext;
import com.fibank.transfer.service.validation.TransferValidationChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the core transfer orchestration. Repositories, the validation chain
 * and the idempotency store are mocked; the real {@link PropertyBasedCurrencyConverter}
 * and {@link LedgerEntryFactory} are used so the FX maths and double-entry creation are
 * genuinely exercised.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TransferServiceImplTest {

    private static final String KEY = "11111111-1111-1111-1111-111111111111";

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TransferRepository transferRepository;
    @Mock
    private LedgerEntryRepository ledgerEntryRepository;
    @Mock
    private TransferValidationChain validationChain;
    @Mock
    private IdempotencyService idempotencyService;

    private final CurrencyConverter currencyConverter =
            new PropertyBasedCurrencyConverter(new BigDecimal("0.86"));
    private final LedgerEntryFactory ledgerEntryFactory = new LedgerEntryFactory();

    private TransferServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new TransferServiceImpl(
                accountRepository, transferRepository, ledgerEntryRepository,
                currencyConverter, validationChain, ledgerEntryFactory, idempotencyService);
        when(transferRepository.save(any(TransferEntity.class))).thenAnswer(returnsFirstArg());
        when(idempotencyService.findExisting(eq(KEY), anyString())).thenReturn(Optional.empty());
    }

    @Test
    void executesSameCurrencyTransfer() {
        AccountEntity source = account("BG01FINV001", Currency.USD, "10000.00");
        AccountEntity destination = account("BG01FINV003", Currency.USD, "2500.00");
        stubLocked(source, destination);

        TransferResult result = service.execute(
                new TransferCommand("BG01FINV001", "BG01FINV003", new BigDecimal("1000.00"), KEY));

        assertThat(source.getBalance()).isEqualByComparingTo("9000.00");
        assertThat(destination.getBalance()).isEqualByComparingTo("3500.00");
        assertThat(result.sourceAmount()).isEqualByComparingTo("1000.00");
        assertThat(result.destinationAmount()).isEqualByComparingTo("1000.00");
        assertThat(result.fxRate()).isEqualByComparingTo("1");
        verify(ledgerEntryRepository).saveAll(any());
        verify(idempotencyService).store(eq(KEY), anyString(), any(TransferResult.class));
    }

    @Test
    void executesCrossCurrencyTransferApplyingFxRate() {
        AccountEntity source = account("BG01FINV001", Currency.USD, "10000.00");
        AccountEntity destination = account("BG01FINV002", Currency.EUR, "5000.00");
        stubLocked(source, destination);

        TransferResult result = service.execute(
                new TransferCommand("BG01FINV001", "BG01FINV002", new BigDecimal("1000.00"), KEY));

        // 1000 USD * 0.86 = 860 EUR
        assertThat(source.getBalance()).isEqualByComparingTo("9000.00");
        assertThat(destination.getBalance()).isEqualByComparingTo("5860.00");
        assertThat(result.sourceCurrency()).isEqualTo(Currency.USD);
        assertThat(result.destinationCurrency()).isEqualTo(Currency.EUR);
        assertThat(result.destinationAmount()).isEqualByComparingTo("860.00");
        assertThat(result.fxRate()).isEqualByComparingTo("0.86");
    }

    @Test
    void failsOnInsufficientFunds() {
        AccountEntity source = account("BG01FINV001", Currency.USD, "100.00");
        AccountEntity destination = account("BG01FINV003", Currency.USD, "2500.00");
        stubLocked(source, destination);
        doThrow(new InsufficientFundsException("BG01FINV001"))
                .when(validationChain).validate(any(TransferContext.class));

        assertThatThrownBy(() -> service.execute(
                new TransferCommand("BG01FINV001", "BG01FINV003", new BigDecimal("1000.00"), KEY)))
                .isInstanceOf(InsufficientFundsException.class);

        assertThat(source.getBalance()).isEqualByComparingTo("100.00"); // unchanged
        verify(transferRepository, never()).save(any());
        verify(idempotencyService, never()).store(anyString(), anyString(), any());
    }

    @Test
    void failsWhenDailyLimitExceeded() {
        AccountEntity source = account("BG01FINV001", Currency.USD, "100000.00");
        AccountEntity destination = account("BG01FINV003", Currency.USD, "2500.00");
        stubLocked(source, destination);
        doThrow(new DailyTransferLimitExceededException("BG01FINV001", new BigDecimal("20000")))
                .when(validationChain).validate(any(TransferContext.class));

        assertThatThrownBy(() -> service.execute(
                new TransferCommand("BG01FINV001", "BG01FINV003", new BigDecimal("25000.00"), KEY)))
                .isInstanceOf(DailyTransferLimitExceededException.class);

        verify(transferRepository, never()).save(any());
    }

    @Test
    void returnsStoredResultOnIdempotentReplay() {
        TransferResult stored = new TransferResult(
                UUID.randomUUID(), "BG01FINV001", "BG01FINV003",
                new BigDecimal("1000.00"), new BigDecimal("1000.00"),
                Currency.USD, Currency.USD, BigDecimal.ONE, null, null);
        when(idempotencyService.findExisting(eq(KEY), anyString())).thenReturn(Optional.of(stored));

        TransferResult result = service.execute(
                new TransferCommand("BG01FINV001", "BG01FINV003", new BigDecimal("1000.00"), KEY));

        assertThat(result).isEqualTo(stored);
        verifyNoInteractions(accountRepository);
        verify(transferRepository, never()).save(any());
        verify(idempotencyService, never()).store(anyString(), anyString(), any());
    }

    private void stubLocked(AccountEntity source, AccountEntity destination) {
        when(accountRepository.findByIbanForUpdate(source.getIban())).thenReturn(Optional.of(source));
        when(accountRepository.findByIbanForUpdate(destination.getIban())).thenReturn(Optional.of(destination));
    }

    private AccountEntity account(String iban, Currency currency, String balance) {
        return new AccountEntity(iban, "Owner " + iban, currency, new BigDecimal(balance));
    }
}
