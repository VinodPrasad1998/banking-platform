package com.nextgenbank.service.impl;

import com.nextgenbank.dto.AccountRequest;
import com.nextgenbank.dto.AccountResponse;
import com.nextgenbank.entity.Account;
import com.nextgenbank.entity.AccountStatus;
import com.nextgenbank.entity.AccountType;
import com.nextgenbank.entity.Customer;
import com.nextgenbank.exception.AccountNotFoundException;
import com.nextgenbank.exception.CustomerNotFoundException;
import com.nextgenbank.repository.AccountRepository;
import com.nextgenbank.repository.CustomerRepository;
import com.nextgenbank.service.AccountService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.nextgenbank.dto.AccountStatusRequest;
import com.nextgenbank.exception.InvalidAccountStatusTransitionException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    public AccountServiceImpl (AccountRepository accountRepository ,CustomerRepository customerRepository)
    {
        this.accountRepository= accountRepository;
        this.customerRepository=customerRepository;
    }

    @Override
    @Transactional
    public AccountResponse createAccount (AccountRequest request)
    {
        Customer customer  =customerRepository.findById(request.customerId()).
                orElseThrow(()->
                        new CustomerNotFoundException("Customer not Found with ID: " + request.customerId()));

        String accountNumber = generateUniqueAccountNumber();

        Account account = new Account();

        account.setAccountNumber(accountNumber);
        account.setAccountType(request.accountType());
        account.setBalance(BigDecimal.ZERO);
        account.setCurrency(
                request.currency() == null ? "INR" : request.currency()
        );
        account.setStatus(AccountStatus.ACTIVE);
        account.setCreatedAt(LocalDateTime.now());
        account.setCustomer(customer);

        Account savedAccount = accountRepository.save(account);

        return mapToResponse(savedAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccountById(Long accountId) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() ->
                        new AccountNotFoundException(
                                "Account not found with ID: " + accountId
                        ));

        return mapToResponse(account);
    }


    @Override
    @Transactional(readOnly = true)
    public Page<AccountResponse> getAllAccounts(
            int page,
            int size,
            String sortBy,
            String direction) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Account> accountPage =
                accountRepository.findAll(pageable);

        return accountPage.map(this::mapToResponse);
    }


    @Override
    @Transactional
    public AccountResponse updateAccountStatus(
            Long accountId,
            AccountStatusRequest request) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() ->
                        new AccountNotFoundException(
                                "Account not found with ID: " + accountId
                        ));

        AccountStatus currentStatus = account.getStatus();
        AccountStatus requestedStatus = request.status();

        if (!isValidStatusTransition(currentStatus, requestedStatus)) {

            throw new InvalidAccountStatusTransitionException(
                    "Invalid account status transition from "
                            + currentStatus
                            + " to "
                            + requestedStatus
            );
        }

        account.setStatus(requestedStatus);

        Account updatedAccount = accountRepository.save(account);

        return mapToResponse(updatedAccount);
    }

    private boolean isValidStatusTransition(
            AccountStatus currentStatus,
            AccountStatus requestedStatus) {

        return switch (currentStatus) {

            case ACTIVE ->
                    requestedStatus == AccountStatus.BLOCKED
                            || requestedStatus == AccountStatus.CLOSED;

            case BLOCKED ->
                    requestedStatus == AccountStatus.ACTIVE
                            || requestedStatus == AccountStatus.CLOSED;

            case CLOSED ->
                    false;
        };
    }

    private String generateUniqueAccountNumber() {

        String accountNumber;

        do {
            accountNumber = String.valueOf(
                    ThreadLocalRandom.current()
                            .nextLong(100000000000L, 999999999999L)
            );
        } while (accountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }

    private AccountResponse mapToResponse(Account account) {

        return new AccountResponse(
                account.getAccountId(),
                account.getAccountNumber(),
                account.getAccountType(),
                account.getBalance(),
                account.getCurrency(),
                account.getStatus(),
                account.getCreatedAt(),
                account.getCustomer().getCustomerId()
        );
    }

}