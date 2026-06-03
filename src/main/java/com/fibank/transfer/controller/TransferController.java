package com.fibank.transfer.controller;

import com.fibank.transfer.dto.request.TransferRequest;
import com.fibank.transfer.dto.response.TransferResponse;
import com.fibank.transfer.mapper.TransferMapper;
import com.fibank.transfer.service.TransferService;
import com.fibank.transfer.service.model.TransferCommand;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transfers")
public class TransferController {

    private final TransferService transferService;
    private final TransferMapper transferMapper;

    public TransferController(TransferService transferService, TransferMapper transferMapper) {
        this.transferService = transferService;
        this.transferMapper = transferMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransferResponse execute(@RequestHeader("X-Idempotency-Key") String idempotencyKey,
                                    @Valid @RequestBody TransferRequest request) {
        TransferCommand command = new TransferCommand(
                request.sourceIban(),
                request.destinationIban(),
                request.amount(),
                idempotencyKey);
        return transferMapper.toResponse(transferService.execute(command));
    }

    @GetMapping("/{id}")
    public TransferResponse getTransfer(@PathVariable UUID id) {
        return transferMapper.toResponse(transferService.getById(id));
    }
}
