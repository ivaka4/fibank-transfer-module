package com.fibank.transfer.controller;

import com.fibank.transfer.dto.request.StandingOrderRequest;
import com.fibank.transfer.dto.response.StandingOrderResponse;
import com.fibank.transfer.mapper.StandingOrderMapper;
import com.fibank.transfer.service.StandingOrderService;
import com.fibank.transfer.service.model.CreateStandingOrderCommand;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/standing-orders")
public class StandingOrderController {

    private final StandingOrderService standingOrderService;
    private final StandingOrderMapper standingOrderMapper;

    public StandingOrderController(StandingOrderService standingOrderService,
                                   StandingOrderMapper standingOrderMapper) {
        this.standingOrderService = standingOrderService;
        this.standingOrderMapper = standingOrderMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StandingOrderResponse create(@Valid @RequestBody StandingOrderRequest request) {
        CreateStandingOrderCommand command = new CreateStandingOrderCommand(
                request.sourceIban(),
                request.destinationIban(),
                request.amount(),
                request.currency(),
                request.cronExpression());
        return standingOrderMapper.toResponse(standingOrderService.create(command));
    }

    @GetMapping
    public List<StandingOrderResponse> listActive() {
        return standingOrderMapper.toResponseList(standingOrderService.findAllActive());
    }

    @GetMapping("/{id}")
    public StandingOrderResponse getById(@PathVariable UUID id) {
        return standingOrderMapper.toResponse(standingOrderService.getById(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable UUID id) {
        standingOrderService.cancel(id);
    }
}
