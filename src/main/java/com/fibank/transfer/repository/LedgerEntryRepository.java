package com.fibank.transfer.repository;

import com.fibank.transfer.entity.LedgerEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Ledger access. Extends {@link JpaSpecificationExecutor} so the audit endpoint
 * can compose optional filters (IBAN, date range, type, amount range) dynamically
 * via the Specification pattern, combined with pagination.
 */
public interface LedgerEntryRepository
        extends JpaRepository<LedgerEntryEntity, Long>, JpaSpecificationExecutor<LedgerEntryEntity> {
}
