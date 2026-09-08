package com.nextgenbank.repository;

import com.nextgenbank.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import java.util.Optional;

import java.util.Collection;
import java.util.List;

public interface AccountRepository extends JpaRepository<Account,Long> {

    boolean existsByAccountNumber(String accountNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Account>findByAccountIdInOrderByAccountIdAsc(
            Collection<Long> accoundIds
    );
    Optional<Account> findByAccountNumber(
            String accountNumber
    );

}
