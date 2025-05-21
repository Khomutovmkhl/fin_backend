package com.example.fin.service.impl;

import com.example.fin.dao.TransactionDao;
import com.example.fin.dao.WalletDao;
import com.example.fin.domain.Transaction;
import com.example.fin.domain.TransactionType;
import com.example.fin.filtering.Filter;
import com.example.fin.filtering.FilterUtils;
import com.example.fin.filtering.Filters;
import com.example.fin.filtering.RestFilterOperator;
import com.example.fin.mapper.dto.TransactionPatchRequestDto;
import com.example.fin.mapper.dto.TransactionPostRequestDto;
import com.example.fin.service.TransactionCategoryService;
import com.example.fin.service.TransactionService;
import com.example.fin.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionDao transactionDao;
    private final UserService userService;
    private final WalletDao walletDao;
    private final TransactionCategoryService transactionCategoryService;

    @Override
    public List<Transaction> getAllTransactions(Filters filters, Pageable pageable) {
        var userId = userService.getCurrentUser().getId().toString();
        filters.getFilters().add(Filter.builder()
                        .filteringAttribute("user.id")
                        .operator(RestFilterOperator.EQ)
                        .values(List.of(userId))
                        .build());
        return transactionDao.findAll(FilterUtils.and(filters), pageable).toList();
    }

    @Override
    public List<Transaction> getAllTransactionsFromWallet(Long walletId) {
        return transactionDao.findAllByWalletId(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Transactions for given wallet were not found"));
    }

    @Override
    public Transaction getOneTransaction(Long transactionId) {
        var userId = userService.getCurrentUser().getId();
        var transaction = transactionDao.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction was not found"));

        if (!transaction.getUser().getId().equals(userId)) {
            throw new ResourceAccessException("You don't have enough permissions");
        }
        return transaction;
    }

    @Override
    public List<Transaction> getAllTransactionsFromCategory(Long transactionCategoryId, Sort sort) {
        var userId = userService.getCurrentUser().getId();
        var category = transactionCategoryService.getOneTransactionCategory(transactionCategoryId);
        if (!category.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("You don't have enough permissions");
        }
        return transactionDao.findAllByTransactionCategoryId(transactionCategoryId, sort)
                .orElseThrow(() -> new ResourceNotFoundException("Transactions were not found"));
    }

    @Override
    public Transaction createTransaction(TransactionPostRequestDto request) {
        var user = userService.getCurrentUser();
        var category = transactionCategoryService.getOneTransactionCategory(request.getTransactionCategoryId());
        if (request.getTransactionType() != category.getTransactionType()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Transaction has different type from category");
        }
        if ((category.getTransactionType() == TransactionType.INCOME && request.getAmount().signum() == -1)
                || (category.getTransactionType() == TransactionType.OUTCOME && request.getAmount().signum() != -1)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid amount for transaction category");
        }
        var walletFromDb = walletDao.findById(request.getWalletId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet with given id was not found"));

        var transaction = Transaction
                .builder()
                .amount(request.getAmount())
                .createdAt(LocalDate.now())
                .note(request.getNote())
                .transactionCategory(category)
                .transactionType(request.getTransactionType())
                .wallet(walletFromDb)
                .user(user)
                .build();

        return transactionDao.save(transaction);
    }

    @Override
    public Transaction updateTransaction(TransactionPatchRequestDto request) {
        var user = userService.getCurrentUser();
        var transaction = transactionDao.findByIdAndUserId(request.getId(), user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction was not found"));
        var wallet = walletDao.findById(request.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet with given id was not found"));
        var category = transactionCategoryService.getOneTransactionCategory(request.getTransactionCategoryId());

        if (request.getTransactionType() != category.getTransactionType()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Transaction has different type from category");
        }

        if (request.getNote() != null) {
            transaction.setNote(request.getNote());
        }
        transaction.setAmount(request.getAmount());
        transaction.setTransactionType(request.getTransactionType());
        transaction.setTransactionCategory(category);
        transaction.setWallet(wallet);

        return transactionDao.save(transaction);
    }

    @Override
    public void deleteTransaction(Long transactionId) {
        var userId = userService.getCurrentUser().getId();
        var transaction = transactionDao.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction was not found"));
        transactionDao.delete(transaction);
    }
}
