package com.fibank.transfer.entity;

import com.fibank.transfer.entity.enums.Currency;
import com.fibank.transfer.entity.enums.StandingOrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.domain.Persistable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * A recurring transfer instruction executed on a cron schedule.
 *
 * <p>Cancellation is a soft delete ({@code active = false}) so history is preserved.
 * A failed run sets {@link StandingOrderStatus#FAILED} but leaves the order active,
 * guaranteeing it is retried on the next scheduled run rather than skipped.
 */
@Entity
@Table(name = "standing_order")
public class StandingOrderEntity implements Persistable<UUID> {

    @Id
    @Column(updatable = false, nullable = false)
    private UUID id;

    /** See {@code TransferEntity}: forces INSERT for the application-assigned UUID id. */
    @Transient
    private boolean isNew = true;

    @Column(name = "source_iban", nullable = false, updatable = false, length = 34)
    private String sourceIban;

    @Column(name = "destination_iban", nullable = false, updatable = false, length = 34)
    private String destinationIban;

    @Column(nullable = false, updatable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 3)
    private Currency currency;

    @Column(name = "cron_expression", nullable = false, length = 64)
    private String cronExpression;

    @Column(nullable = false)
    private boolean active;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "last_run_at")
    private Instant lastRunAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "last_run_status", length = 16)
    private StandingOrderStatus lastRunStatus;

    protected StandingOrderEntity() {
        // required by JPA
    }

    public StandingOrderEntity(String sourceIban, String destinationIban, BigDecimal amount,
                               Currency currency, String cronExpression) {
        this.id = UUID.randomUUID();
        this.sourceIban = sourceIban;
        this.destinationIban = destinationIban;
        this.amount = amount;
        this.currency = currency;
        this.cronExpression = cronExpression;
        this.active = true;
        this.lastRunStatus = StandingOrderStatus.PENDING;
    }

    /** Soft-deletes this order so it is no longer scheduled but remains auditable. */
    public void cancel(Instant when) {
        this.active = false;
        this.cancelledAt = when;
    }

    /** Records the outcome of a scheduled execution. */
    public void recordExecution(StandingOrderStatus status, Instant when) {
        this.lastRunStatus = status;
        this.lastRunAt = when;
    }

    @PostPersist
    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    public String getSourceIban() {
        return sourceIban;
    }

    public String getDestinationIban() {
        return destinationIban;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public Instant getLastRunAt() {
        return lastRunAt;
    }

    public StandingOrderStatus getLastRunStatus() {
        return lastRunStatus;
    }
}
