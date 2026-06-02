package com.fibank.transfer.service.impl;

import com.fibank.transfer.entity.LedgerEntryEntity;
import com.fibank.transfer.repository.LedgerEntryRepository;
import com.fibank.transfer.repository.spec.LedgerFilter;
import com.fibank.transfer.repository.spec.LedgerSpecifications;
import com.fibank.transfer.service.LedgerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class LedgerServiceImpl implements LedgerService {

    private final LedgerEntryRepository ledgerEntryRepository;

    public LedgerServiceImpl(LedgerEntryRepository ledgerEntryRepository) {
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @Override
    public Page<LedgerEntryEntity> query(LedgerFilter filter, Pageable pageable) {
        return ledgerEntryRepository.findAll(LedgerSpecifications.withFilter(filter), pageable);
    }
}
