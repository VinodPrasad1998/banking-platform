package com.nextgenbank.service.impl;

import com.nextgenbank.dto.PaymentRequest;
import com.nextgenbank.dto.PaymentResponse;
import com.nextgenbank.entity.Account;
import com.nextgenbank.entity.Beneficiary;
import com.nextgenbank.entity.Payment;
import com.nextgenbank.exception.InsufficientFundsException;
import com.nextgenbank.repository.AccountRepository;
import com.nextgenbank.repository.BeneficiaryRepository;
import com.nextgenbank.repository.PaymentRepository;
import com.nextgenbank.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PaymentServiceConcurrencyTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private BeneficiaryRepository beneficiaryRepository;

    @Autowired
    private PaymentRepository paymentRepository;


    @Test
    void shouldProcessSamePaymentOnlyOnceWhenRequestsArriveConcurrently()
            throws Exception {

        Long sourceAccountId = 10L;
        Long beneficiaryId = 3L;

        String idempotencyKey =
                "CONCURRENT-" +
                        UUID.randomUUID()
                                .toString()
                                .replace("-", "")
                                .substring(0, 30);

        BigDecimal amount =
                new BigDecimal("100.00");

        Account sourceAccount =
                accountRepository
                        .findById(sourceAccountId)
                        .orElseThrow();

        Beneficiary beneficiary =
                beneficiaryRepository
                        .findById(beneficiaryId)
                        .orElseThrow();

        assertNotNull(beneficiary);

        BigDecimal initialBalance =
                sourceAccount.getBalance();

        PaymentRequest request =
                new PaymentRequest(
                        sourceAccountId,
                        beneficiaryId,
                        amount
                );

        ExecutorService executor =
                Executors.newFixedThreadPool(2);

        CountDownLatch ready =
                new CountDownLatch(2);

        CountDownLatch start =
                new CountDownLatch(1);

        Callable<PaymentResponse> task = () -> {

            ready.countDown();

            start.await();

            return paymentService.createPayment(
                    request,
                    idempotencyKey
            );
        };

        Future<PaymentResponse> future1 =
                executor.submit(task);

        Future<PaymentResponse> future2 =
                executor.submit(task);

        assertTrue(
                ready.await(10, TimeUnit.SECONDS),
                "Both requests did not become ready"
        );

        // Release both requests at approximately the same time.
        start.countDown();

        PaymentResponse response1 = null;
        PaymentResponse response2 = null;

        Throwable exception1 = null;
        Throwable exception2 = null;

        try {
            response1 =
                    future1.get(
                            30,
                            TimeUnit.SECONDS
                    );
        } catch (ExecutionException ex) {
            exception1 = ex.getCause();
        }

        try {
            response2 =
                    future2.get(
                            30,
                            TimeUnit.SECONDS
                    );
        } catch (ExecutionException ex) {
            exception2 = ex.getCause();
        }

        executor.shutdown();

        /*
         * Both requests should succeed and return
         * the same Payment.
         */
        assertNotNull(
                response1,
                "First concurrent request failed: "
                        + exception1
        );

        assertNotNull(
                response2,
                "Second concurrent request failed: "
                        + exception2
        );

        assertEquals(
                response1.paymentId(),
                response2.paymentId(),
                "Concurrent requests created different payments"
        );

        /*
         * Verify the payment exists.
         */
        Payment payment =
                paymentRepository
                        .findByIdempotencyKey(idempotencyKey)
                        .orElseThrow();

        assertEquals(
                "SUCCESS",
                payment.getStatus().name()
        );

        /*
         * Verify the source account was debited
         * exactly once.
         */
        Account updatedAccount =
                accountRepository
                        .findById(sourceAccountId)
                        .orElseThrow();

        assertEquals(
                initialBalance.subtract(amount),
                updatedAccount.getBalance(),
                "Account was debited more than once"
        );

        System.out.println();
        System.out.println("======================================");
        System.out.println("CONCURRENCY TEST RESULT");
        System.out.println("======================================");
        System.out.println("Idempotency Key : " + idempotencyKey);
        System.out.println("Payment ID      : " + payment.getPaymentId());
        System.out.println("Payment Status  : " + payment.getStatus());
        System.out.println("Response 1 ID   : " + response1.paymentId());
        System.out.println("Response 2 ID   : " + response2.paymentId());
        System.out.println("Initial Balance : " + initialBalance);
        System.out.println("Final Balance   : " + updatedAccount.getBalance());
        System.out.println("======================================");
    }


    @Test
    void shouldRollbackPaymentWhenTransferFails()
            throws Exception {

        Long sourceAccountId = 10L;
        Long beneficiaryId = 3L;

        String idempotencyKey =
                "ROLLBACK-" +
                        UUID.randomUUID()
                                .toString()
                                .replace("-", "")
                                .substring(0, 30);

        Account sourceAccount =
                accountRepository
                        .findById(sourceAccountId)
                        .orElseThrow();

        Beneficiary beneficiary =
                beneficiaryRepository
                        .findById(beneficiaryId)
                        .orElseThrow();

        assertNotNull(beneficiary);

        /*
         * Capture the source account's current balance
         * before attempting the payment.
         */
        BigDecimal initialSourceBalance =
                sourceAccount.getBalance();

        /*
         * Resolve the beneficiary's destination account.
         */
        Account destinationAccount =
                accountRepository
                        .findByAccountNumber(
                                beneficiary.getBeneficiaryAccountNumber()
                        )
                        .orElseThrow();

        BigDecimal initialDestinationBalance =
                destinationAccount.getBalance();

        /*
         * Deliberately request more money than
         * the source account contains.
         */
        BigDecimal amount =
                initialSourceBalance.add(
                        new BigDecimal("100.00")
                );

        PaymentRequest request =
                new PaymentRequest(
                        sourceAccountId,
                        beneficiaryId,
                        amount
                );

        /*
         * Payment must fail because the source account
         * does not have enough funds.
         */
        assertThrows(
                InsufficientFundsException.class,
                () -> paymentService.createPayment(
                        request,
                        idempotencyKey
                )
        );

        /*
         * Verify the source account balance
         * was completely rolled back.
         */
        Account sourceAccountAfterFailure =
                accountRepository
                        .findById(sourceAccountId)
                        .orElseThrow();

        assertEquals(
                initialSourceBalance,
                sourceAccountAfterFailure.getBalance(),
                "Source account balance changed even though payment failed"
        );

        /*
         * Verify the destination account balance
         * was not changed.
         */
        Account destinationAccountAfterFailure =
                accountRepository
                        .findById(
                                destinationAccount.getAccountId()
                        )
                        .orElseThrow();

        assertEquals(
                initialDestinationBalance,
                destinationAccountAfterFailure.getBalance(),
                "Destination account balance changed even though payment failed"
        );

        /*
         * Because payment creation and transfer are
         * part of the same transaction, the payment
         * record itself must also be rolled back.
         */
        assertTrue(
                paymentRepository
                        .findByIdempotencyKey(idempotencyKey)
                        .isEmpty(),
                "Payment record exists even though the transaction failed"
        );

        System.out.println();
        System.out.println("======================================");
        System.out.println("ROLLBACK TEST RESULT");
        System.out.println("======================================");
        System.out.println("Idempotency Key          : " + idempotencyKey);
        System.out.println("Initial Source Balance   : " + initialSourceBalance);
        System.out.println("Requested Amount         : " + amount);
        System.out.println("Final Source Balance     : "
                + sourceAccountAfterFailure.getBalance());
        System.out.println("Initial Destination      : "
                + initialDestinationBalance);
        System.out.println("Final Destination        : "
                + destinationAccountAfterFailure.getBalance());
        System.out.println("Payment Persisted        : "
                + paymentRepository
                .findByIdempotencyKey(idempotencyKey)
                .isPresent());
        System.out.println("======================================");
    }
}