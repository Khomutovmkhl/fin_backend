package com.example.fin.service.impl;

import com.example.fin.dao.TransactionCategoryDao;
import com.example.fin.dao.TransactionDao;
import com.example.fin.domain.Transaction;
import com.example.fin.domain.TransactionCategory;
import com.example.fin.mapper.dto.TransactionCategoryPatchRequestDto;
import com.example.fin.service.TransactionCategoryService;
import com.example.fin.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionCategoryServiceImpl implements TransactionCategoryService {

    private final TransactionCategoryDao transactionCategoryDao;
    private final TransactionDao transactionDao;
    private final UserService userService;

    @Override
    public List<TransactionCategory> getAllTransactionCategories(Sort sort) {
        var userId = userService.getCurrentUser().getId();
        var transactions = transactionCategoryDao.findAll(sort);
        transactions.forEach(this::actualizeCategory);
        return transactions;
    }

    @Override
    public TransactionCategory getOneTransactionCategory(Long transactionCategoryId) {
        var category = transactionCategoryDao.findById(transactionCategoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction Category was not found"));
        actualizeCategory(category);
        return category;
    }

    @Override
    public TransactionCategory createTransactionCategory(TransactionCategory transactionCategory) {
        var user = userService.getCurrentUser();
        transactionCategory.setUser(user);
        transactionCategory = transactionCategoryDao.save(transactionCategory);
        actualizeCategory(transactionCategory);
        return transactionCategory;
    }

    @Override
    public TransactionCategory updateTransactionCategory(TransactionCategoryPatchRequestDto request) {
        var transactionCategory = transactionCategoryDao.findById(request.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction Category was not found"));
        if (request.getName() != null) {
            transactionCategory.setName(request.getName());
        }
        transactionCategory = transactionCategoryDao.save(transactionCategory);
        actualizeCategory(transactionCategory);
        return transactionCategory;
    }

    @Override
    public void deleteTransactionCategory(Long transactionCategoryId) {
        var userId = userService.getCurrentUser().getId();
        var transactionCategory = transactionCategoryDao.findById(transactionCategoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction Category was not found"));
        try {
            var transactions = transactionDao.findAllByTransactionCategoryId(transactionCategoryId, Sort.unsorted())
                    .orElseThrow(() -> new ResourceNotFoundException("Transactions in this category were not found"));
            transactionDao.deleteAll(transactions);
        } catch (ResourceNotFoundException ignored) {}
        transactionCategoryDao.delete(transactionCategory);
    }

    private void actualizeCategory(TransactionCategory category) {
        category.setCategoryAmount(calculateCategoryAmount(category));
    }

    private BigDecimal calculateCategoryAmount(TransactionCategory category) {
        var transactions = transactionDao.findAllByTransactionCategoryId(category.getId(), Sort.by("id").ascending())
                .orElseThrow(() -> new ResourceNotFoundException("Transactions were not found"));

        var result = transactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);

        return category.getCategoryAmount() == null ? result : category.getCategoryAmount().add(result);
    }
}
