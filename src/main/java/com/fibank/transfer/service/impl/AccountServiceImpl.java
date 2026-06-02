package com.fibank.transfer.service.impl;

import com.fibank.transfer.entity.AccountEntity;
import com.fibank.transfer.exception.AccountNotFoundException;
import com.fibank.transfer.repository.AccountRepository;
import com.fibank.transfer.service.AccountService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public List<AccountEntity> findAll() {
        return accountRepository.findAll();
    }

    @Override
    public AccountEntity findByIban(String iban) {
        return accountRepository.findByIban(iban)
                .orElseThrow(() -> new AccountNotFoundException(iban));
    }
}
