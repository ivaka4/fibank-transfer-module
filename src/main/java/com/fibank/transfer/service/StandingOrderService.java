package com.fibank.transfer.service;

import com.fibank.transfer.entity.StandingOrderEntity;
import com.fibank.transfer.service.model.CreateStandingOrderCommand;

import java.util.List;
import java.util.UUID;

public interface StandingOrderService {

    StandingOrderEntity create(CreateStandingOrderCommand command);

    List<StandingOrderEntity> findAllActive();

    StandingOrderEntity getById(UUID id);

    /** Soft-deletes (cancels) an active standing order. */
    void cancel(UUID id);

    /** Executes every active standing order whose schedule is due. Invoked by the scheduler. */
    void executeDue();
}
