package com.fibank.transfer.entity;

import com.fibank.transfer.entity.enums.Currency;
import com.fibank.transfer.exception.InsufficientFundsException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * A bank account with a single-currency balance.
 *
 * <p>The balance invariant ("never negative") is enforced here, inside the entity,
 * via {@link #debit(BigDecimal)} — rich behaviour rather than an anemic data holder.
 * The {@code @Version} column provides optimistic-locking as defence-in-depth on top
 * of the pessimistic write-lock taken by the transfer service.
 */
@Entity
@Table(name = "account")
public class AccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false, length = 34)
    private String iban;

    @Column(name = "owner_name", nullable = false)
    private String ownerName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Currency currency;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Version
    @Column(nullable = false)
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected AccountEntity() {
        // required by JPA
    }

    public AccountEntity(String iban, String ownerName, Currency currency, BigDecimal balance) {
        this.iban = iban;
        this.ownerName = ownerName;
        this.currency = currency;
        this.balance = balance;
    }

    /** Returns {@code true} if this account can cover the given amount. */
    public boolean hasSufficientFunds(BigDecimal amount) {
        return balance.compareTo(amount) >= 0;
    }

    /** Withdraws {@code amount}, enforcing the non-negative balance invariant. */
    public void debit(BigDecimal amount) {
        if (!hasSufficientFunds(amount)) {
            throw new InsufficientFundsException(iban);
        }
        this.balance = this.balance.subtract(amount);
    }

    /** Deposits {@code amount} into this account. */
    public void credit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

    public Long getId() {
        return id;
    }

    public String getIban() {
        return iban;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public Currency getCurrency() {
        return currency;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public Long getVersion() {
        return version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
