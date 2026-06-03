package com.fibank.transfer.service.impl;

import com.fibank.transfer.entity.AccountEntity;
import com.fibank.transfer.entity.StandingOrderEntity;
import com.fibank.transfer.entity.enums.StandingOrderStatus;
import com.fibank.transfer.exception.AccountNotFoundException;
import com.fibank.transfer.exception.InvalidTransferException;
import com.fibank.transfer.exception.StandingOrderNotFoundException;
import com.fibank.transfer.repository.AccountRepository;
import com.fibank.transfer.repository.StandingOrderRepository;
import com.fibank.transfer.service.StandingOrderService;
import com.fibank.transfer.service.TransferService;
import com.fibank.transfer.service.model.CreateStandingOrderCommand;
import com.fibank.transfer.service.model.TransferCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class StandingOrderServiceImpl implements StandingOrderService {

    private static final Logger log = LoggerFactory.getLogger(StandingOrderServiceImpl.class);

    private final StandingOrderRepository standingOrderRepository;
    private final AccountRepository accountRepository;
    private final TransferService transferService;
    private final StandingOrderRunRecorder runRecorder;
    private final Clock clock;

    public StandingOrderServiceImpl(StandingOrderRepository standingOrderRepository,
                                    AccountRepository accountRepository,
                                    TransferService transferService,
                                    StandingOrderRunRecorder runRecorder,
                                    Clock clock) {
        this.standingOrderRepository = standingOrderRepository;
        this.accountRepository = accountRepository;
        this.transferService = transferService;
        this.runRecorder = runRecorder;
        this.clock = clock;
    }

    @Override
    @Transactional
    public StandingOrderEntity create(CreateStandingOrderCommand command) {
        AccountEntity source = accountRepository.findByIban(command.sourceIban())
                .orElseThrow(() -> new AccountNotFoundException(command.sourceIban()));
        if (!accountRepository.existsByIban(command.destinationIban())) {
            throw new AccountNotFoundException(command.destinationIban());
        }
        if (command.currency() != source.getCurrency()) {
            throw new InvalidTransferException(
                    "Standing order currency %s must match source account currency %s"
                            .formatted(command.currency(), source.getCurrency()));
        }
        if (!CronExpression.isValidExpression(command.cronExpression())) {
            throw new InvalidTransferException("Invalid cron expression: " + command.cronExpression());
        }

        StandingOrderEntity order = new StandingOrderEntity(
                command.sourceIban(),
                command.destinationIban(),
                command.amount(),
                command.currency(),
                command.cronExpression());
        return standingOrderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StandingOrderEntity> findAllActive() {
        return standingOrderRepository.findByActiveTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public StandingOrderEntity getById(UUID id) {
        return standingOrderRepository.findById(id)
                .orElseThrow(() -> new StandingOrderNotFoundException(id));
    }

    @Override
    @Transactional
    public void cancel(UUID id) {
        StandingOrderEntity order = standingOrderRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new StandingOrderNotFoundException(id));
        order.cancel(Instant.now(clock));
    }

    /**
     * Iterates active orders and executes those whose cron schedule is due. Each order
     * is independent: the transfer runs in its own transaction and the run status is
     * recorded in a separate one, so a single failure neither aborts the batch nor
     * deactivates the order — it is retried on the next run.
     */
    @Override
    public void executeDue() {
        Instant now = Instant.now(clock);
        ZonedDateTime nowZdt = ZonedDateTime.now(clock);

        for (StandingOrderEntity order : standingOrderRepository.findByActiveTrue()) {
            if (!isDue(order, nowZdt)) {
                continue;
            }
            StandingOrderStatus status;
            try {
                TransferCommand command = new TransferCommand(
                        order.getSourceIban(),
                        order.getDestinationIban(),
                        order.getAmount(),
                        UUID.randomUUID().toString());
                transferService.execute(command);
                status = StandingOrderStatus.SUCCESS;
                log.info("Standing order {} executed successfully", order.getId());
            } catch (RuntimeException e) {
                status = StandingOrderStatus.FAILED;
                log.error("Standing order {} failed: {} — will be retried on the next run",
                        order.getId(), e.getMessage());
            }
            runRecorder.record(order.getId(), status, now);
        }
    }

    private boolean isDue(StandingOrderEntity order, ZonedDateTime now) {
        Instant reference = order.getLastRunAt() != null ? order.getLastRunAt() : order.getCreatedAt();
        ZonedDateTime referenceZdt = ZonedDateTime.ofInstant(reference, clock.getZone());
        ZonedDateTime next = CronExpression.parse(order.getCronExpression()).next(referenceZdt);
        return next != null && !next.isAfter(now);
    }
}
