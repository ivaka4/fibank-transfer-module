package com.fibank.transfer.service;

import com.fibank.transfer.entity.LedgerEntryEntity;
import com.fibank.transfer.repository.spec.LedgerFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LedgerService {

    /** Returns a paginated, filtered view of the ledger (filters combined with AND). */
    Page<LedgerEntryEntity> query(LedgerFilter filter, Pageable pageable);
}
