package com.fibank.transfer.service;

import com.fibank.transfer.entity.AccountEntity;

import java.util.List;

public interface AccountService {

    List<AccountEntity> findAll();

    AccountEntity findByIban(String iban);
}
