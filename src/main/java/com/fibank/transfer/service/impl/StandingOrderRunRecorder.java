package com.fibank.transfer.service.impl;

import com.fibank.transfer.entity.enums.StandingOrderStatus;
import com.fibank.transfer.repository.StandingOrderRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Records the outcome of a standing-order run in its own transaction.
 *
 * <p>Deliberately a separate bean (and {@code REQUIRES_NEW}) so the status write
 * commits independently of the transfer transaction. A failed transfer rolls back
 * its own work, yet the {@code FAILED} status is still persisted — the order stays
 * active and is retried on the next run rather than silently skipped.
 */
@Component
public class StandingOrderRunRecorder {

    private final StandingOrderRepository standingOrderRepository;

    public StandingOrderRunRecorder(StandingOrderRepository standingOrderRepository) {
        this.standingOrderRepository = standingOrderRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(UUID standingOrderId, StandingOrderStatus status, Instant when) {
        standingOrderRepository.findById(standingOrderId)
                .ifPresent(order -> order.recordExecution(status, when));
    }
}
