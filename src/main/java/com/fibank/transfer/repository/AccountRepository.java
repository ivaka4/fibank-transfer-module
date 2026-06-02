package com.fibank.transfer.repository;

import com.fibank.transfer.entity.AccountEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<AccountEntity, Long> {

    Optional<AccountEntity> findByIban(String iban);

    /**
     * Loads an account taking a pessimistic write-lock (SELECT ... FOR UPDATE).
     * Used by the transfer service to serialise concurrent transfers touching the
     * same account, preventing lost updates on the balance. Callers must acquire
     * locks in a deterministic order (by IBAN) to avoid deadlocks.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AccountEntity a where a.iban = :iban")
    Optional<AccountEntity> findByIbanForUpdate(@Param("iban") String iban);

    boolean existsByIban(String iban);
}
