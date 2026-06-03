package com.fibank.transfer.controller;

import com.fibank.transfer.dto.response.AccountResponse;
import com.fibank.transfer.mapper.AccountMapper;
import com.fibank.transfer.service.AccountService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;
    private final AccountMapper accountMapper;

    public AccountController(AccountService accountService, AccountMapper accountMapper) {
        this.accountService = accountService;
        this.accountMapper = accountMapper;
    }

    @GetMapping
    public List<AccountResponse> listAccounts() {
        return accountMapper.toResponseList(accountService.findAll());
    }

    @GetMapping("/{iban}")
    public AccountResponse getAccount(@PathVariable String iban) {
        return accountMapper.toResponse(accountService.findByIban(iban));
    }
}
