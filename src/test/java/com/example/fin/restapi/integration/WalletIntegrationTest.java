package com.example.fin.restapi.integration;

import com.example.fin.dao.TransactionCategoryDao;
import com.example.fin.dao.TransactionDao;
import com.example.fin.dao.UserDao;
import com.example.fin.dao.WalletDao;
import com.example.fin.domain.Currency;
import com.example.fin.mapper.dto.WalletPatchRequestDto;
import com.example.fin.mapper.dto.WalletPostRequestDto;
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

public class WalletIntegrationTest extends IntegrationTest {
    private final String URL = "wallets";

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
    void getAllWallets() throws Exception {
        var expectations = new LinkedList<ResultMatcher>();
        expectations.add(jsonPath("$", hasSize(2)));
        expectations.add(jsonPath("$[*].id", contains(walletA.getId().intValue(), walletB.getId().intValue())));

        performGet(URL, userToken)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpectAll(expectations.toArray(new ResultMatcher[0]));
    }

    @Test
    void getOneWallet() throws Exception {
        var expectations = new LinkedList<ResultMatcher>();
        expectations.add(jsonPath("$.id", equalTo(walletA.getId().intValue())));
        expectations.add(jsonPath("$.name", equalTo("walletA")));
        expectations.add(jsonPath("$.balance", equalTo(1500.0)));
        expectations.add(jsonPath("$.deposit", equalTo(1000.0)));
        expectations.add(jsonPath("$.currency", equalTo("CZK")));

        performGet(URL + "/" + walletA.getId(), userToken)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpectAll(expectations.toArray(new ResultMatcher[0]));
    }

    @Test
    void postWallet() throws Exception {
        var expectations = new LinkedList<ResultMatcher>();
        expectations.add(jsonPath("$.name", equalTo("Created wallet")));
        expectations.add(jsonPath("$.balance", equalTo(500)));
        expectations.add(jsonPath("$.deposit", equalTo(500)));
        expectations.add(jsonPath("$.currency", equalTo("CZK")));

        var postWallet = WalletPostRequestDto.builder()
                .name("Created wallet")
                .deposit(new BigDecimal("500"))
                .currency(Currency.CZK)
                .build();

        performPost(URL, postWallet, userToken)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpectAll(expectations.toArray(new ResultMatcher[0]));
    }

    @Test
    void patchWallet() throws Exception {
        var expectations = new LinkedList<ResultMatcher>();
        expectations.add(jsonPath("$.name", equalTo("Updated wallet")));
        expectations.add(jsonPath("$.balance", equalTo(1000.0)));
        expectations.add(jsonPath("$.deposit", equalTo(500)));
        expectations.add(jsonPath("$.currency", equalTo("CZK")));

        var patchWallet = WalletPatchRequestDto.builder()
                .id(walletA.getId())
                .name("Updated wallet")
                .deposit(new BigDecimal("500"))
                .build();

        performPatch(URL, patchWallet, userToken)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpectAll(expectations.toArray(new ResultMatcher[0]));
    }

    @Test
    void deleteWallet() throws Exception {
        boolean thrown = false;
        performDelete(URL, walletA.getId(), userToken)
                .andDo(print())
                .andExpect(status().isOk());
        try {
            walletDao.findById(walletA.getId())
                    .orElseThrow(() -> new RuntimeException("Wallet was not found"));
        } catch (RuntimeException ex) {
            thrown = true;
        }
        assertTrue(thrown);
    }
}
