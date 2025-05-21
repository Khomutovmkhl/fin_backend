package com.example.fin.service.impl;

import com.example.fin.dao.WalletDao;
import com.example.fin.domain.Wallet;
import com.example.fin.filtering.Filter;
import com.example.fin.filtering.FilterUtils;
import com.example.fin.filtering.Filters;
import com.example.fin.filtering.RestFilterOperator;
import com.example.fin.mapper.dto.WalletPatchRequestDto;
import com.example.fin.service.TransactionService;
import com.example.fin.service.UserService;
import com.example.fin.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletDao walletDao;
    private final UserService userService;
    private final TransactionService transactionService;

    @Override
    public List<Wallet> getAllWallets(Filters filters, Pageable pageable) {
//        var userId = userService.getCurrentUser().getId().toString();
//        filters.getFilters().add(Filter.builder()
//                .filteringAttribute("user.id")
//                .operator(RestFilterOperator.EQ)
//                .values(List.of(userId))
//                .build());

        var wallets = walletDao.findAll(FilterUtils.and(filters), pageable).toList();
        wallets.forEach(this::actualizeWallet);
        return wallets;
    }

    @Override
    public Wallet getOneWallet(Long walletId) {
        var userId = userService.getCurrentUser().getId();
        var walletFromDb = walletDao.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet with given id was not found"));
        actualizeWallet(walletFromDb);
        return walletFromDb;
    }

    @Override
    public Wallet createWallet(Wallet wallet) {
        var user = userService.getCurrentUser();
        wallet.setUser(user);
        wallet = walletDao.save(wallet);
        actualizeWallet(wallet);
        return wallet;
    }

    @Override
    public Wallet updateWallet(WalletPatchRequestDto request) {
        var userId = userService.getCurrentUser().getId();
        var walletFromDb = walletDao.findById(request.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet with given id was not found"));
        if (request.getName() != null) {
            walletFromDb.setName(request.getName());
        }
        if (request.getDeposit() != null) {
            walletFromDb.setDeposit(request.getDeposit());
        }
        walletFromDb = walletDao.save(walletFromDb);
        actualizeWallet(walletFromDb);
        return walletFromDb;
    }

    @Override
    public void deleteWallet(Long walletId) {
        var userId = userService.getCurrentUser().getId();
        var walletFromDb = walletDao.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet with given id was not found"));
        try {
            transactionService.getAllTransactionsFromWallet(walletId)
                    .forEach(transaction -> transactionService.deleteTransaction(transaction.getId()));
        } catch (ResourceNotFoundException ignored) {}
        walletDao.delete(walletFromDb);
    }

    private void actualizeWallet(Wallet wallet) {
        wallet.setBalance(calculateBalance(wallet));
    }


    private BigDecimal calculateBalance(Wallet wallet) {
        var transactions = transactionService.getAllTransactionsFromWallet(wallet.getId());
        var result = transactions.stream()
                .map(transaction -> transaction.isIncome() ? transaction.getAmount() : transaction.getAmount().negate())
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
        return wallet.getDeposit().add(result);
    }
}
