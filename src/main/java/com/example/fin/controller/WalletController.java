package com.example.fin.controller;

import com.example.fin.filtering.FilteringFields;
import com.example.fin.filtering.Filters;
import com.example.fin.mapper.DtoMapper;
import com.example.fin.mapper.dto.TransactionFullDto;
import com.example.fin.mapper.dto.WalletFullDto;
import com.example.fin.mapper.dto.WalletPatchRequestDto;
import com.example.fin.mapper.dto.WalletPostRequestDto;
import com.example.fin.paging.SortingFields;
import com.example.fin.service.TransactionService;
import com.example.fin.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final TransactionService transactionService;
    private final DtoMapper dtoMapper;

    @GetMapping
    @SortingFields(value = {"id", "name", "deposit"}, defaultSort = "id")
    @FilteringFields(value = {"id", "name", "deposit", "currency", "transactions", "user"})
    public ResponseEntity<List<WalletFullDto>> getAllWallets(Filters filters, Pageable pageable) {
        return ResponseEntity.ok(
                walletService.getAllWallets(filters, pageable)
                        .stream()
                        .map(dtoMapper::walletToWalletFullDto)
                        .toList()
        );
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<TransactionFullDto>> getAllTransactionsFromOneWallets(@Valid @PathVariable("id") Long walletId) {
        return ResponseEntity.ok(
                transactionService.getAllTransactionsFromWallet(walletId)
                        .stream()
                        .map(dtoMapper::transactionToTransactionFullDto)
                        .toList()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<WalletFullDto> getOneWallet(@Valid @PathVariable("id") Long walletId) {
        return ResponseEntity.ok(
                dtoMapper.walletToWalletFullDto(
                        walletService.getOneWallet(walletId)
                )
        );
    }

    @PostMapping
    public ResponseEntity<WalletFullDto> createWallet(@Valid @RequestBody WalletPostRequestDto request) {
            return ResponseEntity.ok(
                dtoMapper.walletToWalletFullDto(
                        walletService.createWallet(
                                dtoMapper.walletPostRequestDtoToWallet(request)
                        )
                )
        );
    }

    @PatchMapping
    public ResponseEntity<WalletFullDto> updateWallet(@Valid @RequestBody WalletPatchRequestDto request) {
        return ResponseEntity.ok(
                dtoMapper.walletToWalletFullDto(
                        walletService.updateWallet(request)
                )
        );
    }

    @DeleteMapping("/{id}")
    public void deleteWallet(@Valid @PathVariable Long id) {
        walletService.deleteWallet(id);
    }

    private static Sort getSort(String[] sort) {
        if (!((sort[0].equals("id")
                || sort[0].equals("name")
                || sort[0].equals("deposit")
                || sort[0].equals("timePeriod"))
                &&
                (sort[1].equals("desc")
                || sort[1].equals("asc")))
        ) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not correct sorting parameters");
        }
        Sort sorting;
        if (sort[1].equals("desc")) {
            sorting = Sort.by(sort[0]).descending();
        }else {
            sorting = Sort.by(sort[0]).ascending();
        }
        return sorting;
    }
}
