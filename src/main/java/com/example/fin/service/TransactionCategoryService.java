package com.example.fin.service;

import com.example.fin.domain.TransactionCategory;
import com.example.fin.mapper.dto.TransactionCategoryPatchRequestDto;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface TransactionCategoryService {
    List<TransactionCategory> getAllTransactionCategories(Sort sort); // GET ALL
    TransactionCategory getOneTransactionCategory(Long transactionCategoryId); // GET One
    TransactionCategory createTransactionCategory(TransactionCategory transactionCategory); // POST
    TransactionCategory updateTransactionCategory(TransactionCategoryPatchRequestDto request); // PATCH
    void deleteTransactionCategory(Long transactionCategoryId); // DELETE
}
