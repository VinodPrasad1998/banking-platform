package com.nextgenbank.service;

import com.nextgenbank.dto.*;
import org.springframework.data.domain.Page;

public interface TransactionService {

    TransactionResponse deposit(DepositRequest request);
    Page<TransactionResponse> getTransactionHistory(Long accountId ,int page,int size,String sortBy,String direction);
    TransactionResponse withdraw(WithdrawalRequest request);
    TransferResponse transfer(TransferRequest request);
    TransactionResponse getTransactionById (Long transactionId);
}