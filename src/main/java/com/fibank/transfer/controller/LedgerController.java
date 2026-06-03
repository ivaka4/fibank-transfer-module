package com.fibank.transfer.controller;

import com.fibank.transfer.dto.response.LedgerEntryResponse;
import com.fibank.transfer.entity.enums.TransactionType;
import com.fibank.transfer.mapper.LedgerMapper;
import com.fibank.transfer.repository.spec.LedgerFilter;
import com.fibank.transfer.service.LedgerService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Read-only audit endpoint over the ledger. All filters are optional and combined
 * with AND via the Specification pattern; results are paginated (default size 20).
 */
@RestController
@RequestMapping("/api/v1/ledger")
public class LedgerController {

    private final LedgerService ledgerService;
    private final LedgerMapper ledgerMapper;

    public LedgerController(LedgerService ledgerService, LedgerMapper ledgerMapper) {
        this.ledgerService = ledgerService;
        this.ledgerMapper = ledgerMapper;
    }

    @GetMapping
    public PagedModel<LedgerEntryResponse> queryLedger(
            @RequestParam(required = false) String accountIban,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant dateTo,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @PageableDefault(size = 20) Pageable pageable) {

        LedgerFilter filter = new LedgerFilter(accountIban, dateFrom, dateTo, type, minAmount, maxAmount);
        return new PagedModel<>(ledgerService.query(filter, pageable).map(ledgerMapper::toResponse));
    }
}
