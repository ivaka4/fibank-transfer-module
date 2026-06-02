package com.fibank.transfer.service.factory;

import com.fibank.transfer.entity.LedgerEntryEntity;
import com.fibank.transfer.entity.TransferEntity;
import com.fibank.transfer.entity.enums.TransactionType;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Factory for the double-entry ledger pair. Centralising creation here guarantees
 * the invariant — every transfer yields exactly one DEBIT on the source and one
 * CREDIT on the destination — in a single place that cannot be bypassed.
 */
@Component
public class LedgerEntryFactory {

    /** Builds the DEBIT (source) and CREDIT (destination) entries for a transfer. */
    public List<LedgerEntryEntity> createDoubleEntry(TransferEntity transfer, String correlationId) {
        LedgerEntryEntity debit = new LedgerEntryEntity(
                transfer.getSourceIban(),
                transfer.getId(),
                TransactionType.DEBIT,
                transfer.getSourceAmount(),
                transfer.getSourceCurrency(),
                correlationId);

        LedgerEntryEntity credit = new LedgerEntryEntity(
                transfer.getDestinationIban(),
                transfer.getId(),
                TransactionType.CREDIT,
                transfer.getDestinationAmount(),
                transfer.getDestinationCurrency(),
                correlationId);

        return List.of(debit, credit);
    }
}
