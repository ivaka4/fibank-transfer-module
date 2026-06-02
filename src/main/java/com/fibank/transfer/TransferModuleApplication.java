package com.fibank.transfer;

import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point of the Inter-Account Transfer & Standing Order module.
 *
 * <p>{@code @EnableScheduling} activates the cron-driven standing-order job, while
 * {@code @EnableSchedulerLock} (ShedLock) guarantees that in a multi-instance
 * deployment the job runs on a single instance at a time.
 */
@SpringBootApplication
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT30S")
public class TransferModuleApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransferModuleApplication.class, args);
    }
}
