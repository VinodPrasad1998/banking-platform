package com.nextgenbank.service;

import com.nextgenbank.dto.AccountRequest;
import com.nextgenbank.dto.AccountResponse;

public interface AccountService {
    AccountResponse createAccount (AccountRequest request);
    AccountResponse getAccountById(Long accountId);
}
