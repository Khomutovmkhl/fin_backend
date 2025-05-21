package com.example.fin.restapi;

import com.example.fin.auth.AuthenticationService;
import com.example.fin.auth.RegisterRequest;
import com.example.fin.dao.TransactionCategoryDao;
import com.example.fin.dao.TransactionDao;
import com.example.fin.dao.UserDao;
import com.example.fin.dao.WalletDao;
import com.example.fin.domain.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;


@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IntegrationTest {

    @Autowired
    protected MockMvc mockMvc;
    protected String userToken;
    protected User user;
    protected String adminToken;
    protected User admin;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ObjectWriter objectWriter = objectMapper.writer();
    @Autowired
    private WalletDao walletDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    private TransactionDao transactionDao;
    @Autowired
    private TransactionCategoryDao transactionCategoryDao;
    @Autowired
    private AuthenticationService authenticationService;
    private final String BASE_URL = "/api/v1/";

    protected Wallet walletA, walletB;
    protected Transaction incomeTransaction, outcomeTransaction;
    protected TransactionCategory incomeCategory, outcomeCategory;

    protected void prepareUsers() {
        userDao.deleteAll();
        var userRequest = RegisterRequest.builder()
                .firstName("user")
                .lastName("FIN")
                .email("user@gmail.com")
                .password("user")
                .build();
        var adminRequest = RegisterRequest.builder()
                .firstName("admin")
                .lastName("FIN")
                .email("admin@gmail.com")
                .password("admin")
                .build();
        userToken = authenticationService.register(userRequest).getToken();
        adminToken = authenticationService.register(adminRequest).getToken();
        user = userDao.findByEmail("user@gmail.com")
                .orElseThrow();
        admin = userDao.findByEmail("admin@gmail.com")
                .orElseThrow();
    }

    protected void prepareTransactions() {
        incomeTransaction = Transaction.builder()
                .note("Income")
                .amount(new BigDecimal("1000"))
                .transactionType(TransactionType.INCOME)
                .transactionCategory(incomeCategory)
                .wallet(walletA)
                .createdAt(LocalDate.now())
                .user(user)
                .build();
        incomeTransaction = transactionDao.save(incomeTransaction);

        outcomeTransaction = Transaction.builder()
                .note("Outcome")
                .amount(new BigDecimal("500"))
                .transactionType(TransactionType.OUTCOME)
                .transactionCategory(outcomeCategory)
                .wallet(walletA)
                .createdAt(LocalDate.now())
                .user(user)
                .build();
        outcomeTransaction = transactionDao.save(outcomeTransaction);
    }

    protected void prepareCategories() {
        incomeCategory = TransactionCategory.builder()
                .user(user)
                .name("Income Category")
                .transactionType(TransactionType.INCOME)
                .build();
        incomeCategory = transactionCategoryDao.save(incomeCategory);

        outcomeCategory = TransactionCategory.builder()
                .user(user)
                .name("Outcome Category")
                .transactionType(TransactionType.OUTCOME)
                .build();
        outcomeCategory = transactionCategoryDao.save(outcomeCategory);
    }

    protected void prepareWallets() {
        walletA = Wallet.builder()
                .name("walletA")
                .deposit(new BigDecimal("1000"))
                .currency(Currency.CZK)
                .user(user)
//                .timePeriod(LocalDate.now())
                .build();
        walletA = walletDao.save(walletA);

        walletB = Wallet.builder()
                .name("walletB")
                .deposit(new BigDecimal("2000"))
                .currency(Currency.EUR)
                .user(user)
//                .timePeriod(LocalDate.now())
                .build();
        walletB = walletDao.save(walletB);
    }

    protected ResultActions performGet(String url, String token) throws Exception {
        url = BASE_URL + url;
        return mockMvc.perform(get(url)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    protected ResultActions performPost(String url, Object payload, String token) throws Exception {
        String content = objectWriter.writeValueAsString(payload);
        url = BASE_URL + url;
        return mockMvc.perform(post(url)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(content));
    }
    protected ResultActions performPost(String url, Object payload) throws Exception {
        String content = objectWriter.writeValueAsString(payload);
        url = BASE_URL + url;
        return mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(content));
    }

    protected ResultActions performPatch(String url, Object payload, String token) throws Exception {
        String content = objectWriter.writeValueAsString(payload);
        url = BASE_URL + url;
        return mockMvc.perform(patch(url)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(content));
    }

    protected ResultActions performDelete(String url, Long id, String token) throws Exception {
        url = BASE_URL + url + "/" + id;
        return mockMvc.perform(delete(url)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON));
    }
}
