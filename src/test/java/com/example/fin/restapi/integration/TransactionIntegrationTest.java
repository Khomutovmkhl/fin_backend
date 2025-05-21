package com.example.fin.restapi.integration;

import com.example.fin.dao.TransactionCategoryDao;
import com.example.fin.dao.TransactionDao;
import com.example.fin.dao.UserDao;
import com.example.fin.dao.WalletDao;
import com.example.fin.domain.TransactionType;
import com.example.fin.mapper.dto.TransactionPatchRequestDto;
import com.example.fin.mapper.dto.TransactionPostRequestDto;
import com.example.fin.restapi.IntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultMatcher;

import java.math.BigDecimal;
import java.util.LinkedList;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TransactionIntegrationTest extends IntegrationTest {
    private final String URL = "transactions";

    @Autowired
    private WalletDao walletDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    private TransactionDao transactionDao;
    @Autowired
    private TransactionCategoryDao transactionCategoryDao;

    @BeforeEach
    void setUp() {
        prepareUsers();
        prepareWallets();
        prepareCategories();
        prepareTransactions();
    }

    @AfterEach
    void tearDown() {
        transactionDao.deleteAll();
        transactionCategoryDao.deleteAll();
        walletDao.deleteAll();
        userDao.deleteAll();
    }

    @Test
    void getAllTransactions() throws Exception {
        var expectations = new LinkedList<ResultMatcher>();
        expectations.add(jsonPath("$", hasSize(2)));
        expectations.add(jsonPath("$[*].id", containsInAnyOrder(incomeTransaction.getId().intValue(), outcomeTransaction.getId().intValue())));

        performGet(URL, userToken)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpectAll(expectations.toArray(new ResultMatcher[0]));
    }

    @Test
    void getAllTransactionsFromWallet() throws Exception {
        var expectations = new LinkedList<ResultMatcher>();
        expectations.add(jsonPath("$", hasSize(2)));
        expectations.add(jsonPath("$[*].id", containsInAnyOrder(incomeTransaction.getId().intValue(), outcomeTransaction.getId().intValue())));

        performGet("wallets/" + walletA.getId().toString() + "/" + URL, userToken)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpectAll(expectations.toArray(new ResultMatcher[0]));
    }

    @Test
    void getAllTransactionsFromCategory() throws Exception {
        var expectations = new LinkedList<ResultMatcher>();
        expectations.add(jsonPath("$", hasSize(1)));
        expectations.add(jsonPath("$[*].id", containsInAnyOrder(incomeTransaction.getId().intValue())));

        performGet("categories/" + incomeCategory.getId().toString() + "/" + URL, userToken)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpectAll(expectations.toArray(new ResultMatcher[0]));
    }

    @Test
    void getOneTransaction() throws Exception {
        var expectations = new LinkedList<ResultMatcher>();
        expectations.add(jsonPath("$.note", equalTo(incomeTransaction.getNote())));
        expectations.add(jsonPath("$.amount", equalTo(1000.0)));
        expectations.add(jsonPath("$.transactionType", equalTo(TransactionType.INCOME.name())));
        expectations.add(jsonPath("$.transactionCategoryId", equalTo(incomeCategory.getId().intValue())));
        expectations.add(jsonPath("$.transactionCategoryName", equalTo(incomeCategory.getName())));

        performGet(URL + "/" + incomeTransaction.getId(), userToken)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpectAll(expectations.toArray(new ResultMatcher[0]));
    }

    @Test
    void postTransaction() throws Exception {
        var expectations = new LinkedList<ResultMatcher>();
        expectations.add(jsonPath("$.note", equalTo("Created transaction")));
        expectations.add(jsonPath("$.amount", equalTo(500)));
        expectations.add(jsonPath("$.transactionType", equalTo(TransactionType.INCOME.name())));
        expectations.add(jsonPath("$.transactionCategoryId", equalTo(incomeCategory.getId().intValue())));
        expectations.add(jsonPath("$.transactionCategoryName", equalTo(incomeCategory.getName())));

        var postTransaction = TransactionPostRequestDto.builder()
                .note("Created transaction")
                .amount(new BigDecimal("500"))
                .transactionCategoryId(incomeCategory.getId())
                .transactionType(TransactionType.INCOME)
                .walletId(walletA.getId())
                .build();

        performPost(URL, postTransaction, userToken)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpectAll(expectations.toArray(new ResultMatcher[0]));
    }

    @Test
    void patchTransaction() throws Exception {
        var expectations = new LinkedList<ResultMatcher>();
        expectations.add(jsonPath("$.id", equalTo(incomeTransaction.getId().intValue())));
        expectations.add(jsonPath("$.note", equalTo("Updated OUTCOME transaction")));
        expectations.add(jsonPath("$.amount", equalTo(500)));
        expectations.add(jsonPath("$.transactionType", equalTo(TransactionType.OUTCOME.name())));
        expectations.add(jsonPath("$.transactionCategoryId", equalTo(outcomeCategory.getId().intValue())));
        expectations.add(jsonPath("$.transactionCategoryName", equalTo(outcomeCategory.getName())));

        var patchTransaction = TransactionPatchRequestDto.builder()
                .id(incomeTransaction.getId())
                .note("Updated OUTCOME transaction")
                .amount(new BigDecimal("500"))
                .transactionType(TransactionType.OUTCOME)
                .transactionCategoryId(outcomeCategory.getId())
                .walletId(walletA.getId())
                .build();

        performPatch(URL, patchTransaction, userToken)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpectAll(expectations.toArray(new ResultMatcher[0]));
    }

    @Test
    void deleteTransaction() throws Exception {
        boolean thrown = false;
        performDelete(URL, incomeTransaction.getId(), userToken)
                .andDo(print())
                .andExpect(status().isOk());
        try {
            transactionDao.findById(incomeTransaction.getId())
                    .orElseThrow(() -> new RuntimeException("Transaction was not found"));
        } catch (RuntimeException ex) {
            thrown = true;
        }
        assertTrue(thrown);
    }
}
