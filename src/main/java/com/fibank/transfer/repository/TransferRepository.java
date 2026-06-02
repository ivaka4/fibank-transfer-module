package com.fibank.transfer.repository;

import com.fibank.transfer.entity.TransferEntity;
import com.fibank.transfer.entity.enums.TransferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface TransferRepository extends JpaRepository<TransferEntity, UUID> {

    /**
     * Sums the source amounts of all completed outgoing transfers for an account
     * within a time window. Used to enforce the per-account, per-calendar-day
     * transfer limit. {@code COALESCE} returns zero when there are no transfers.
     */
    @Query("""
            select coalesce(sum(t.sourceAmount), 0)
            from TransferEntity t
            where t.sourceIban = :iban
              and t.status = :status
              and t.createdAt >= :from
              and t.createdAt < :to
            """)
    BigDecimal sumOutgoingBetween(@Param("iban") String iban,
                                  @Param("status") TransferStatus status,
                                  @Param("from") Instant from,
                                  @Param("to") Instant to);
}
