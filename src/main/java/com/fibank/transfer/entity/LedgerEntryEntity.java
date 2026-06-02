package com.fibank.transfer.entity;

import com.fibank.transfer.entity.enums.Currency;
import com.fibank.transfer.entity.enums.TransactionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * An append-only ledger entry — one half of a double-entry pair. Immutable by
 * design: it exposes no setters and is never updated or deleted, which keeps the
 * ledger a trustworthy audit trail.
 */
@Entity
@Table(name = "ledger_entry")
public class LedgerEntryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_iban", nullable = false, updatable = false, length = 34)
    private String accountIban;

    @Column(name = "transfer_id", nullable = false, updatable = false)
    private UUID transferId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 6)
    private TransactionType type;

    @Column(nullable = false, updatable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 3)
    private Currency currency;

    @Column(name = "correlation_id", updatable = false, length = 36)
    private String correlationId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected LedgerEntryEntity() {
        // required by JPA
    }

    public LedgerEntryEntity(String accountIban, UUID transferId, TransactionType type,
                             BigDecimal amount, Currency currency, String correlationId) {
        this.accountIban = accountIban;
        this.transferId = transferId;
        this.type = type;
        this.amount = amount;
        this.currency = currency;
        this.correlationId = correlationId;
    }

    public Long getId() {
        return id;
    }

    public String getAccountIban() {
        return accountIban;
    }

    public UUID getTransferId() {
        return transferId;
    }

    public TransactionType getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
