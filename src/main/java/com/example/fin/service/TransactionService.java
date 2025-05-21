package com.example.fin.service;

import com.example.fin.domain.Transaction;
import com.example.fin.filtering.Filters;
import com.example.fin.mapper.dto.TransactionPatchRequestDto;
import com.example.fin.mapper.dto.TransactionPostRequestDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface TransactionService {
    List<Transaction> getAllTransactions(Filters filters, Pageable pageable); // GET ALL for user

    List<Transaction> getAllTransactionsFromWallet(Long walletId); // GET ALL from one wallet

    Transaction getOneTransaction(Long transactionId); // GET One

    List<Transaction> getAllTransactionsFromCategory(Long transactionCategoryId, Sort sort); // GET ALL from transaction category

    Transaction createTransaction(TransactionPostRequestDto request); // POST

    Transaction updateTransaction(TransactionPatchRequestDto request); // PATCH

    void deleteTransaction(Long transactionId); // DELETE
}
