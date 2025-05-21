package com.example.fin.dao;

import com.example.fin.domain.Transaction;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface TransactionDao extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {
    Optional<List<Transaction>> findAllByWalletId(Long walletId);
    Optional<List<Transaction>> findAllByTransactionCategoryId(Long transactionCategoryId, Sort sort);
    Optional<Transaction> findByIdAndUserId(Long id, Long userId);
}
