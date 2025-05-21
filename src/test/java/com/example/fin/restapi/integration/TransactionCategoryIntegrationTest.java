package com.example.fin.restapi.integration;

import com.example.fin.dao.TransactionCategoryDao;
import com.example.fin.dao.TransactionDao;
import com.example.fin.dao.UserDao;
import com.example.fin.dao.WalletDao;
import com.example.fin.domain.TransactionType;
import com.example.fin.mapper.dto.TransactionCategoryPatchRequestDto;
import com.example.fin.mapper.dto.TransactionCategoryPostRequestDto;
import com.example.fin.restapi.IntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.LinkedList;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TransactionCategoryIntegrationTest extends IntegrationTest {
    private final String URL = "categories";

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
    void getAllTCategories() throws Exception {
        var expectations = new LinkedList<ResultMatcher>();
        expectations.add(jsonPath("$", hasSize(2)));
        expectations.add(jsonPath("$[*].id", containsInAnyOrder(incomeCategory.getId().intValue(), outcomeCategory.getId().intValue())));

        performGet(URL, userToken)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpectAll(expectations.toArray(new ResultMatcher[0]));
    }

    @Test
    void getOneCategory() throws Exception {
        var expectations = new LinkedList<ResultMatcher>();
        expectations.add(jsonPath("$.id", equalTo(incomeCategory.getId().intValue())));
        expectations.add(jsonPath("$.name", equalTo(incomeCategory.getName())));
        expectations.add(jsonPath("$.transactionType", equalTo(incomeCategory.getTransactionType().name())));
        expectations.add(jsonPath("$.categoryAmount", equalTo(1000.0))); // From incomeTransaction

        performGet(URL + "/" + incomeCategory.getId(), userToken)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpectAll(expectations.toArray(new ResultMatcher[0]));
    }

    @Test
    void postTransaction() throws Exception {
        var expectations = new LinkedList<ResultMatcher>();
        expectations.add(jsonPath("$.name", equalTo("Created category")));
        expectations.add(jsonPath("$.transactionType", equalTo(TransactionType.OUTCOME.name())));
        expectations.add(jsonPath("$.categoryAmount", equalTo(0))); // From incomeTransaction

        var postCategory = TransactionCategoryPostRequestDto.builder()
                .name("Created category")
                .transactionType(TransactionType.OUTCOME)
                .build();

        performPost(URL, postCategory, userToken)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpectAll(expectations.toArray(new ResultMatcher[0]));
    }

    @Test
    void patchCategory() throws Exception {
        var expectations = new LinkedList<ResultMatcher>();
        expectations.add(jsonPath("$.id", equalTo(incomeCategory.getId().intValue())));
        expectations.add(jsonPath("$.name", equalTo("Updated category")));
        expectations.add(jsonPath("$.transactionType", equalTo(incomeCategory.getTransactionType().name())));
        expectations.add(jsonPath("$.categoryAmount", equalTo(1000.0))); // From incomeTransaction

        var patchCategory = TransactionCategoryPatchRequestDto.builder()
                .id(incomeCategory.getId())
                .name("Updated category")
                .build();

        performPatch(URL, patchCategory, userToken)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpectAll(expectations.toArray(new ResultMatcher[0]));
    }

    @Test
    void deleteCategory() throws Exception {
        boolean thrown = false;
        performDelete(URL, incomeCategory.getId(), userToken)
                .andDo(print())
                .andExpect(status().isOk());
        try {
            transactionCategoryDao.findById(incomeCategory.getId())
                    .orElseThrow(() -> new RuntimeException("Category was not found"));
        } catch (RuntimeException ex) {
            thrown = true;
        }
        assertTrue(thrown);
    }
}
