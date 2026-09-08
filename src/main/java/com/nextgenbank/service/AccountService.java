package com.nextgenbank.service;

import com.nextgenbank.dto.AccountRequest;
import com.nextgenbank.dto.AccountResponse;
import com.nextgenbank.dto.AccountStatusRequest;
import org.springframework.data.domain.Page;

public interface AccountService {
    AccountResponse createAccount (AccountRequest request);
    AccountResponse getAccountById(Long accountId);
    Page<AccountResponse> getAllAccounts(
            int page,
            int size,
            String sortBy,
            String direction
    );
    AccountResponse updateAccountStatus(
            Long accountId,
            AccountStatusRequest request
    );
}
