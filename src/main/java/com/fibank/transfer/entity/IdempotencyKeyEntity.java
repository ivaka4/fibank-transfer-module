package com.fibank.transfer.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.UUID;

/**
 * Stores the outcome of a processed transfer request keyed by its client-supplied
 * {@code X-Idempotency-Key}. A replay of the same key returns the stored response
 * without re-executing the transfer. Entries expire 24h after creation.
 *
 * <p>{@code requestHash} lets us detect a key reused with a different payload and
 * reject it (409), rather than silently returning an unrelated response.
 */
@Entity
@Table(name = "idempotency_key")
public class IdempotencyKeyEntity implements Persistable<String> {

    @Id
    @Column(name = "idempotency_key", length = 36)
    private String key;

    /** Forces INSERT for the application-assigned key (avoids a SELECT-then-merge). */
    @Transient
    private boolean isNew = true;

    @Column(name = "request_hash", nullable = false, length = 64)
    private String requestHash;

    @Column(name = "response_status", nullable = false)
    private int responseStatus;

    @Lob
    @Column(name = "response_body", nullable = false)
    private String responseBody;

    @Column(name = "transfer_id")
    private UUID transferId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false, updatable = false)
    private Instant expiresAt;

    protected IdempotencyKeyEntity() {
        // required by JPA
    }

    public IdempotencyKeyEntity(String key, String requestHash, int responseStatus, String responseBody,
                                UUID transferId, Instant createdAt, Instant expiresAt) {
        this.key = key;
        this.requestHash = requestHash;
        this.responseStatus = responseStatus;
        this.responseBody = responseBody;
        this.transferId = transferId;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public boolean isExpired(Instant now) {
        return now.isAfter(expiresAt);
    }

    @PostPersist
    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }

    @Override
    public String getId() {
        return key;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    public String getKey() {
        return key;
    }

    public String getRequestHash() {
        return requestHash;
    }

    public int getResponseStatus() {
        return responseStatus;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public UUID getTransferId() {
        return transferId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}
