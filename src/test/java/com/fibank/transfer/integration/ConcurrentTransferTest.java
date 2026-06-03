package com.fibank.transfer.integration;

import com.fibank.transfer.repository.AccountRepository;
import com.fibank.transfer.service.TransferService;
import com.fibank.transfer.service.model.TransferCommand;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Proves the pessimistic-locking strategy prevents lost updates under real concurrency.
 *
 * <p>Many threads attempt to transfer from the same account simultaneously, requesting
 * far more than the balance allows. Without correct locking some updates would be lost
 * (money created/destroyed) or the balance would go negative. The assertions verify the
 * money-conservation invariant, the non-negative balance, and that the final balance
 * matches exactly the number of transfers that succeeded.
 */
@SpringBootTest(properties = "fib.api-key=test-api-key")
class ConcurrentTransferTest {

    @Autowired
    private TransferService transferService;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void concurrentTransfersFromSameAccountDoNotLoseUpdates() throws InterruptedException {
        String source = "BG01FINV002"; // EUR, untouched by other tests
        String destination = "BG01FINV004"; // EUR
        BigDecimal amount = new BigDecimal("1000.00");
        int threadCount = 12; // 12 x 1000 = 12000 requested, but the balance covers far fewer

        BigDecimal sourceStart = balanceOf(source);
        BigDecimal destinationStart = balanceOf(destination);
        BigDecimal totalStart = sourceStart.add(destinationStart);

        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch go = new CountDownLatch(1);
        AtomicInteger successes = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            pool.submit(() -> {
                ready.countDown();
                try {
                    go.await(); // release all threads at once for maximum contention
                    transferService.execute(
                            new TransferCommand(source, destination, amount, UUID.randomUUID().toString()));
                    successes.incrementAndGet();
                } catch (RuntimeException expected) {
                    // transfers that would overdraw the account are rejected — expected
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        ready.await();
        go.countDown();
        pool.shutdown();
        assertThat(pool.awaitTermination(30, TimeUnit.SECONDS)).isTrue();

        BigDecimal sourceEnd = balanceOf(source);
        BigDecimal destinationEnd = balanceOf(destination);

        // 1) Money is conserved — nothing created or lost.
        assertThat(sourceEnd.add(destinationEnd)).isEqualByComparingTo(totalStart);
        // 2) The balance never went negative.
        assertThat(sourceEnd).isGreaterThanOrEqualTo(BigDecimal.ZERO);
        // 3) The final balance matches exactly the successful transfers (no lost update).
        BigDecimal expectedSource =
                sourceStart.subtract(amount.multiply(BigDecimal.valueOf(successes.get())));
        assertThat(sourceEnd).isEqualByComparingTo(expectedSource);
    }

    private BigDecimal balanceOf(String iban) {
        return accountRepository.findByIban(iban).orElseThrow().getBalance();
    }
}
