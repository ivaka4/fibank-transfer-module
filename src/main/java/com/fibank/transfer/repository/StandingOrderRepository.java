package com.fibank.transfer.repository;

import com.fibank.transfer.entity.StandingOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StandingOrderRepository extends JpaRepository<StandingOrderEntity, UUID> {

    /** Active (non-cancelled) standing orders — the set the scheduler iterates over. */
    List<StandingOrderEntity> findByActiveTrue();

    Optional<StandingOrderEntity> findByIdAndActiveTrue(UUID id);
}
