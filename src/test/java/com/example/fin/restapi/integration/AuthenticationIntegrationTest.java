package com.example.fin.restapi.integration;

import com.example.fin.auth.AuthenticationRequest;
import com.example.fin.auth.RegisterRequest;
import com.example.fin.dao.TransactionCategoryDao;
import com.example.fin.dao.TransactionDao;
import com.example.fin.dao.UserDao;
import com.example.fin.dao.WalletDao;
import com.example.fin.restapi.IntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.LinkedList;

import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthenticationIntegrationTest extends IntegrationTest {
    private final String URL = "auth";

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
    void registerUser() throws Exception {
        var expectations = new LinkedList<ResultMatcher>();
        expectations.add(jsonPath("$", aMapWithSize(1)));
        expectations.add(jsonPath("$.token", notNullValue()));

        var registerUser = RegisterRequest.builder()
                .firstName("Created user")
                .lastName("Created user")
                .email("created@gmail.com")
                .password("created")
                .build();

        performPost(URL + "/register", registerUser)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpectAll(expectations.toArray(new ResultMatcher[0]));
    }

    @Test
    void authenticateUser() throws Exception {
        var expectations = new LinkedList<ResultMatcher>();
        expectations.add(jsonPath("$", aMapWithSize(1)));
        expectations.add(jsonPath("$.token", notNullValue()));

        var authenticateUser = AuthenticationRequest.builder()
                .email(user.getEmail())
                .password("user")
                .build();

        performPost(URL + "/authenticate", authenticateUser)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpectAll(expectations.toArray(new ResultMatcher[0]));
    }
}
