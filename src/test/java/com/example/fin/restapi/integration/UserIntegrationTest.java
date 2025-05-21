package com.example.fin.restapi.integration;

import com.example.fin.dao.TransactionCategoryDao;
import com.example.fin.dao.TransactionDao;
import com.example.fin.dao.UserDao;
import com.example.fin.dao.WalletDao;
import com.example.fin.mapper.dto.UserPatchRequestDto;
import com.example.fin.restapi.IntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.LinkedList;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserIntegrationTest extends IntegrationTest {
    private final String URL = "user";

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
    void getUser() throws Exception {
        var expectations = new LinkedList<ResultMatcher>();
        expectations.add(jsonPath("$.id", equalTo(user.getId().intValue())));
        expectations.add(jsonPath("$.firstName", equalTo(user.getFirstName())));
        expectations.add(jsonPath("$.lastName", equalTo(user.getLastName())));
        expectations.add(jsonPath("$.email", equalTo(user.getEmail())));

        performGet(URL, userToken)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpectAll(expectations.toArray(new ResultMatcher[0]));
    }

    @Test
    void patchUser() throws Exception {
        var expectations = new LinkedList<ResultMatcher>();
        expectations.add(jsonPath("$.id", equalTo(user.getId().intValue())));
        expectations.add(jsonPath("$.firstName", equalTo("Updated user")));
        expectations.add(jsonPath("$.lastName", equalTo("Updated user")));
        expectations.add(jsonPath("$.email", equalTo(user.getEmail())));

        var patchWallet = UserPatchRequestDto.builder()
                .firstName("Updated user")
                .lastName("Updated user")
                .build();

        performPatch(URL, patchWallet, userToken)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpectAll(expectations.toArray(new ResultMatcher[0]));
    }
}
