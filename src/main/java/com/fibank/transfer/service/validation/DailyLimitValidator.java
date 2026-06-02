package com.fibank.transfer.service.validation;

import com.fibank.transfer.entity.enums.TransferStatus;
import com.fibank.transfer.exception.DailyTransferLimitExceededException;
import com.fibank.transfer.repository.TransferRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Enforces the per-account, per-calendar-day outgoing transfer limit. The already
 * transferred amount for the current day is summed from completed transfers and
 * combined with the requested amount. Uses an injected {@link Clock} so the
 * "current day" is deterministic and testable.
 */
@Component
@Order(4)
public class DailyLimitValidator implements TransferValidator {

    private final TransferRepository transferRepository;
    private final Clock clock;
    private final BigDecimal dailyLimit;

    public DailyLimitValidator(TransferRepository transferRepository,
                               Clock clock,
                               @Value("${transfer.daily-limit}") BigDecimal dailyLimit) {
        this.transferRepository = transferRepository;
        this.clock = clock;
        this.dailyLimit = dailyLimit;
    }

    @Override
    public void validate(TransferContext context) {
        String iban = context.source().getIban();

        ZonedDateTime now = ZonedDateTime.now(clock);
        Instant dayStart = now.toLocalDate().atStartOfDay(clock.getZone()).toInstant();
        Instant dayEnd = dayStart.plus(1, ChronoUnit.DAYS);

        BigDecimal alreadyTransferred =
                transferRepository.sumOutgoingBetween(iban, TransferStatus.COMPLETED, dayStart, dayEnd);

        if (alreadyTransferred.add(context.amount()).compareTo(dailyLimit) > 0) {
            throw new DailyTransferLimitExceededException(iban, dailyLimit);
        }
    }
}
