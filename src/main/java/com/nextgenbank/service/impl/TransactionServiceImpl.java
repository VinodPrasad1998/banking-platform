package com.nextgenbank.service.impl;

import com.nextgenbank.dto.DepositRequest;
import com.nextgenbank.dto.TransactionResponse;
import com.nextgenbank.entity.Account;
import com.nextgenbank.entity.AccountStatus;
import com.nextgenbank.entity.Transaction;
import com.nextgenbank.entity.TransactionType;
import com.nextgenbank.exception.AccountNotActiveException;
import com.nextgenbank.exception.AccountNotFoundException;
import com.nextgenbank.repository.AccountRepository;
import com.nextgenbank.repository.TransactionRepository;
import com.nextgenbank.service.TransactionService;
import com.nextgenbank.entity.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.nextgenbank.dto.WithdrawalRequest;
import com.nextgenbank.exception.InsufficientFundsException;
import com.nextgenbank.dto.TransferRequest;
import com.nextgenbank.dto.TransferResponse;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public TransactionServiceImpl(
            AccountRepository accountRepository,
            TransactionRepository transactionRepository) {

        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional
    public TransactionResponse deposit(DepositRequest request) {

        Account account = accountRepository.findById(request.accountId())
                .orElseThrow(() ->
                        new AccountNotFoundException(
                                "Account not found with ID: " + request.accountId()
                        )
                );

        if (account.getStatus() != AccountStatus.ACTIVE) {

            throw new AccountNotActiveException(
                    "Deposit is not allowed because account status is "
                            + account.getStatus()
            );
        }

        BigDecimal currentBalance = account.getBalance();

        BigDecimal newBalance = currentBalance.add(request.amount());

        account.setBalance(newBalance);

        accountRepository.save(account);

        Transaction transaction = new Transaction();

        transaction.setTransactionReference(
                generateTransactionReference()
        );

        transaction.setAccount(account);
        transaction.setTransactionType(TransactionType.DEPOSIT);
        transaction.setAmount(request.amount());
        transaction.setBalanceAfter(newBalance);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setCreatedAt(LocalDateTime.now());

        Transaction savedTransaction =
                transactionRepository.save(transaction);

        return mapToResponse(savedTransaction);
    }

    private String generateTransactionReference() {

        String reference;

        do {
            reference = "TXN-" +
                    UUID.randomUUID()
                            .toString()
                            .replace("-", "")
                            .substring(0, 20)
                            .toUpperCase();

        } while (
                transactionRepository.existsByTransactionReference(reference)
        );

        return reference;
    }


    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactionHistory(
            Long accountId,
            int page,
            int size,
            String sortBy,
            String direction) {

        if (!accountRepository.existsById(accountId)) {
            throw new AccountNotFoundException(
                    "Account not found with ID: " + accountId
            );
        }

        if (!isAllowedTransactionSortField(sortBy)) {
            throw new IllegalArgumentException(
                    "Invalid transaction sort field: " + sortBy
            );
        }

        if (!direction.equalsIgnoreCase("asc")
                && !direction.equalsIgnoreCase("desc")) {

            throw new IllegalArgumentException(
                    "Invalid sort direction: " + direction
            );
        }
        Sort.Direction sortDirection =
                Sort.Direction.fromString(direction);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, sortBy)
        );

        return transactionRepository
                .findByAccountAccountId(accountId, pageable)
                .map(this::mapToResponse);
    }


    @Override
    @Transactional
    public TransactionResponse withdraw(WithdrawalRequest request) {

        Account account = accountRepository.findById(request.accountId())
                .orElseThrow(() -> new AccountNotFoundException(
                        "Account not found with ID: " + request.accountId()
                ));

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException(
                    "Withdrawal is not allowed because account status is "
                            + account.getStatus()
            );
        }

        BigDecimal currentBalance = account.getBalance();

        if (request.amount().compareTo(currentBalance) > 0) {
            throw new InsufficientFundsException(
                    "Insufficient funds. Available balance: "
                            + currentBalance
                            + ", requested amount: "
                            + request.amount()
            );
        }

        BigDecimal newBalance = currentBalance.subtract(request.amount());

        account.setBalance(newBalance);
        accountRepository.save(account);

        Transaction transaction = new Transaction();

        transaction.setTransactionReference(generateTransactionReference());
        transaction.setAccount(account);
        transaction.setTransactionType(TransactionType.WITHDRAWAL);
        transaction.setAmount(request.amount());
        transaction.setBalanceAfter(newBalance);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setCreatedAt(LocalDateTime.now());

        Transaction savedTransaction =
                transactionRepository.save(transaction);

        return mapToResponse(savedTransaction);
    }


    @Override
    @Transactional
    public TransferResponse transfer(TransferRequest request) {

        if (request.sourceAccountId()
                .equals(request.destinationAccountId())) {

            throw new IllegalArgumentException(
                    "Source and destination accounts must be different"
            );
        }

        List<Account> accounts =
                accountRepository.findByAccountIdInOrderByAccountIdAsc(
                        Arrays.asList(
                                request.sourceAccountId(),
                                request.destinationAccountId()
                        )
                );

        if (accounts.size() != 2) {

            throw new AccountNotFoundException(
                    "One or both accounts were not found"
            );
        }

        Account sourceAccount = accounts.stream()
                .filter(account ->
                        account.getAccountId()
                                .equals(request.sourceAccountId()))
                .findFirst()
                .orElseThrow(() -> new AccountNotFoundException(
                        "Source account not found with ID: "
                                + request.sourceAccountId()
                ));

        Account destinationAccount = accounts.stream()
                .filter(account ->
                        account.getAccountId()
                                .equals(request.destinationAccountId()))
                .findFirst()
                .orElseThrow(() -> new AccountNotFoundException(
                        "Destination account not found with ID: "
                                + request.destinationAccountId()
                ));

        if (sourceAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException(
                    "Transfer is not allowed because source account status is "
                            + sourceAccount.getStatus()
            );
        }

        if (destinationAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException(
                    "Transfer is not allowed because destination account status is "
                            + destinationAccount.getStatus()
            );
        }

        if (request.amount().compareTo(sourceAccount.getBalance()) > 0) {
            throw new InsufficientFundsException(
                    "Insufficient funds. Available balance: "
                            + sourceAccount.getBalance()
                            + ", requested amount: "
                            + request.amount()
            );
        }

        BigDecimal sourceNewBalance =
                sourceAccount.getBalance()
                        .subtract(request.amount());

        BigDecimal destinationNewBalance =
                destinationAccount.getBalance()
                        .add(request.amount());

        sourceAccount.setBalance(sourceNewBalance);
        destinationAccount.setBalance(destinationNewBalance);

        accountRepository.save(sourceAccount);
        accountRepository.save(destinationAccount);

        String transferReference = generateTransferReference();

        Transaction sourceTransaction = new Transaction();
        sourceTransaction.setTransactionReference(
                generateTransactionReference()
        );
        sourceTransaction.setTransferReference(transferReference);
        sourceTransaction.setAccount(sourceAccount);
        sourceTransaction.setTransactionType(TransactionType.WITHDRAWAL);
        sourceTransaction.setAmount(request.amount());
        sourceTransaction.setBalanceAfter(sourceNewBalance);
        sourceTransaction.setStatus(TransactionStatus.SUCCESS);
        sourceTransaction.setCreatedAt(LocalDateTime.now());

        Transaction destinationTransaction = new Transaction();
        destinationTransaction.setTransactionReference(
                generateTransactionReference()
        );
        destinationTransaction.setTransferReference(transferReference);
        destinationTransaction.setAccount(destinationAccount);
        destinationTransaction.setTransactionType(TransactionType.DEPOSIT);
        destinationTransaction.setAmount(request.amount());
        destinationTransaction.setBalanceAfter(destinationNewBalance);
        destinationTransaction.setStatus(TransactionStatus.SUCCESS);
        destinationTransaction.setCreatedAt(LocalDateTime.now());

        Transaction savedSourceTransaction =
                transactionRepository.save(sourceTransaction);

        Transaction savedDestinationTransaction =
                transactionRepository.save(destinationTransaction);

        return new TransferResponse(
                transferReference,
                mapToResponse(savedSourceTransaction),
                mapToResponse(savedDestinationTransaction)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(Long transactionId) {

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException(
                        "Transaction not found with ID: " + transactionId
                ));

        return mapToResponse(transaction);
    }

    private boolean isAllowedTransactionSortField(String sortBy) {

        return sortBy.equals("transactionId")
                || sortBy.equals("transactionReference")
                || sortBy.equals("transactionType")
                || sortBy.equals("amount")
                || sortBy.equals("balanceAfter")
                || sortBy.equals("createdAt");
    }

    private String generateTransferReference() {

        return "TRF-" +
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 20)
                        .toUpperCase();
    }

    private TransactionResponse mapToResponse(Transaction transaction) {

        Account account = transaction.getAccount();

        return new TransactionResponse(
                transaction.getTransactionId(),
                transaction.getTransactionReference(),
                transaction.getTransferReference(),
                account.getAccountId(),
                account.getAccountNumber(),
                transaction.getTransactionType(),
                transaction.getAmount(),
                transaction.getBalanceAfter(),
                account.getCurrency(),
                transaction.getStatus(),
                transaction.getCreatedAt()
        );
    }
}