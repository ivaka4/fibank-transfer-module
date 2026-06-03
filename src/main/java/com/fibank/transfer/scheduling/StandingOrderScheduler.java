package com.fibank.transfer.scheduling;

import com.fibank.transfer.service.StandingOrderService;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Cron-driven trigger for standing orders. {@code @SchedulerLock} ensures that across
 * a multi-instance deployment only one instance runs the job per tick; the per-order
 * due/retry logic lives in {@link StandingOrderService#executeDue()}.
 */
@Component
public class StandingOrderScheduler {

    private static final Logger log = LoggerFactory.getLogger(StandingOrderScheduler.class);

    private final StandingOrderService standingOrderService;

    public StandingOrderScheduler(StandingOrderService standingOrderService) {
        this.standingOrderService = standingOrderService;
    }

    @Scheduled(cron = "${standing-order.cron}")
    @SchedulerLock(name = "standingOrderJob", lockAtMostFor = "PT30S", lockAtLeastFor = "PT1S")
    public void runDueStandingOrders() {
        log.debug("Standing-order job tick — scanning for due orders");
        standingOrderService.executeDue();
    }
}
