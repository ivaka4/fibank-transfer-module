package com.fibank.transfer.service.impl;

import tools.jackson.databind.ObjectMapper;
import com.fibank.transfer.entity.IdempotencyKeyEntity;
import com.fibank.transfer.exception.IdempotencyConflictException;
import com.fibank.transfer.repository.IdempotencyKeyRepository;
import com.fibank.transfer.service.IdempotencyService;
import com.fibank.transfer.service.model.TransferResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.Optional;

/**
 * JDBC-backed idempotency store. The request fingerprint is hashed (SHA-256) so a
 * key replayed with a different payload is detected and rejected, while an identical
 * replay returns the stored result.
 */
@Service
public class IdempotencyServiceImpl implements IdempotencyService {

    private final IdempotencyKeyRepository repository;
    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final int ttlHours;

    public IdempotencyServiceImpl(IdempotencyKeyRepository repository,
                                  ObjectMapper objectMapper,
                                  Clock clock,
                                  @Value("${idempotency.ttl-hours}") int ttlHours) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.clock = clock;
        this.ttlHours = ttlHours;
    }

    @Override
    @Transactional
    public Optional<TransferResult> findExisting(String key, String fingerprint) {
        Optional<IdempotencyKeyEntity> found = repository.findById(key);
        if (found.isEmpty()) {
            return Optional.empty();
        }

        IdempotencyKeyEntity entity = found.get();

        // Expired entries are treated as absent and removed so the key can be reused.
        if (entity.isExpired(Instant.now(clock))) {
            repository.delete(entity);
            return Optional.empty();
        }

        if (!entity.getRequestHash().equals(hash(fingerprint))) {
            throw new IdempotencyConflictException(key);
        }

        return Optional.of(deserialize(entity.getResponseBody()));
    }

    @Override
    @Transactional
    public void store(String key, String fingerprint, TransferResult result) {
        Instant now = Instant.now(clock);
        IdempotencyKeyEntity entity = new IdempotencyKeyEntity(
                key,
                hash(fingerprint),
                201,
                serialize(result),
                result.id(),
                now,
                now.plus(ttlHours, ChronoUnit.HOURS));
        repository.save(entity);
    }

    private String serialize(TransferResult result) {
        try {
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize idempotent response", e);
        }
    }

    private TransferResult deserialize(String body) {
        try {
            return objectMapper.readValue(body, TransferResult.class);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize idempotent response", e);
        }
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
