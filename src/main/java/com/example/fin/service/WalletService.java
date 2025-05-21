package com.example.fin.service;

import com.example.fin.domain.Wallet;
import com.example.fin.filtering.Filters;
import com.example.fin.mapper.dto.WalletPatchRequestDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface WalletService {
    List<Wallet> getAllWallets(Filters filters, Pageable pageable); // GET ALL

    Wallet getOneWallet(Long walletId); // GET One

    Wallet createWallet(Wallet wallet); // POST

    Wallet updateWallet(WalletPatchRequestDto request); // PATCH

    void deleteWallet(Long walletId); // DELETE

}
