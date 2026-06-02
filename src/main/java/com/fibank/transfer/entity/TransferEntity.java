package com.fibank.transfer.entity;

import com.fibank.transfer.entity.enums.Currency;
import com.fibank.transfer.entity.enums.TransferStatus;
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
 * An immutable record of an executed transfer (the "transaction" aggregate).
 * Stores both source and destination amounts/currencies plus the applied FX rate,
 * so a cross-currency transfer is fully auditable from a single row.
 *
 * <p>Constructed via {@link Builder} — the field count makes a builder far more
 * readable and less error-prone than a long positional constructor.
 */
@Entity
@Table(name = "transfer")
public class TransferEntity implements Persistable<UUID> {

    @Id
    @Column(updatable = false, nullable = false)
    private UUID id;

    /**
     * Marks the entity as new so Spring Data issues an INSERT (persist) rather than a
     * SELECT-then-merge for our application-assigned UUID id. Reset once the row exists,
     * which also lets the {@code @CreationTimestamp} populate before we read it back.
     */
    @Transient
    private boolean isNew = true;

    @Column(name = "source_iban", nullable = false, updatable = false, length = 34)
    private String sourceIban;

    @Column(name = "destination_iban", nullable = false, updatable = false, length = 34)
    private String destinationIban;

    @Column(name = "source_amount", nullable = false, updatable = false, precision = 19, scale = 2)
    private BigDecimal sourceAmount;

    @Column(name = "destination_amount", nullable = false, updatable = false, precision = 19, scale = 2)
    private BigDecimal destinationAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_currency", nullable = false, updatable = false, length = 3)
    private Currency sourceCurrency;

    @Enumerated(EnumType.STRING)
    @Column(name = "destination_currency", nullable = false, updatable = false, length = 3)
    private Currency destinationCurrency;

    @Column(name = "fx_rate", nullable = false, updatable = false, precision = 19, scale = 6)
    private BigDecimal fxRate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 16)
    private TransferStatus status;

    @Column(name = "idempotency_key", updatable = false, length = 36)
    private String idempotencyKey;

    @Column(name = "correlation_id", updatable = false, length = 36)
    private String correlationId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected TransferEntity() {
        // required by JPA
    }

    private TransferEntity(Builder b) {
        this.id = b.id;
        this.sourceIban = b.sourceIban;
        this.destinationIban = b.destinationIban;
        this.sourceAmount = b.sourceAmount;
        this.destinationAmount = b.destinationAmount;
        this.sourceCurrency = b.sourceCurrency;
        this.destinationCurrency = b.destinationCurrency;
        this.fxRate = b.fxRate;
        this.status = b.status;
        this.idempotencyKey = b.idempotencyKey;
        this.correlationId = b.correlationId;
    }

    public static Builder builder() {
        return new Builder();
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

    public BigDecimal getSourceAmount() {
        return sourceAmount;
    }

    public BigDecimal getDestinationAmount() {
        return destinationAmount;
    }

    public Currency getSourceCurrency() {
        return sourceCurrency;
    }

    public Currency getDestinationCurrency() {
        return destinationCurrency;
    }

    public BigDecimal getFxRate() {
        return fxRate;
    }

    public TransferStatus getStatus() {
        return status;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public static final class Builder {
        private UUID id = UUID.randomUUID();
        private String sourceIban;
        private String destinationIban;
        private BigDecimal sourceAmount;
        private BigDecimal destinationAmount;
        private Currency sourceCurrency;
        private Currency destinationCurrency;
        private BigDecimal fxRate;
        private TransferStatus status;
        private String idempotencyKey;
        private String correlationId;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder sourceIban(String sourceIban) {
            this.sourceIban = sourceIban;
            return this;
        }

        public Builder destinationIban(String destinationIban) {
            this.destinationIban = destinationIban;
            return this;
        }

        public Builder sourceAmount(BigDecimal sourceAmount) {
            this.sourceAmount = sourceAmount;
            return this;
        }

        public Builder destinationAmount(BigDecimal destinationAmount) {
            this.destinationAmount = destinationAmount;
            return this;
        }

        public Builder sourceCurrency(Currency sourceCurrency) {
            this.sourceCurrency = sourceCurrency;
            return this;
        }

        public Builder destinationCurrency(Currency destinationCurrency) {
            this.destinationCurrency = destinationCurrency;
            return this;
        }

        public Builder fxRate(BigDecimal fxRate) {
            this.fxRate = fxRate;
            return this;
        }

        public Builder status(TransferStatus status) {
            this.status = status;
            return this;
        }

        public Builder idempotencyKey(String idempotencyKey) {
            this.idempotencyKey = idempotencyKey;
            return this;
        }

        public Builder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public TransferEntity build() {
            return new TransferEntity(this);
        }
    }
}
