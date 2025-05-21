package com.example.fin.controller;

import com.example.fin.filtering.FilteringFields;
import com.example.fin.filtering.Filters;
import com.example.fin.mapper.DtoMapper;
import com.example.fin.mapper.dto.TransactionFullDto;
import com.example.fin.mapper.dto.TransactionPatchRequestDto;
import com.example.fin.mapper.dto.TransactionPostRequestDto;
import com.example.fin.paging.SortingFields;
import com.example.fin.service.TransactionService;
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
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final DtoMapper dtoMapper;

    @GetMapping
    @SortingFields(value = {"id", "amount", "createdAt"}, defaultSort = "-createdAt")
    @FilteringFields(value = {"id", "amount", "createdAt", "note", "transactionType", "wallet"})
    public ResponseEntity<List<TransactionFullDto>> getAllTransactions(Filters filters,
                                                                       Pageable pageable) {
        return ResponseEntity.ok(
                transactionService.getAllTransactions(filters, pageable)
                        .stream()
                        .map(dtoMapper::transactionToTransactionFullDto)
                        .toList()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionFullDto> getOneTransaction(@PathVariable Long id) {
        return ResponseEntity.ok(dtoMapper.transactionToTransactionFullDto(
                transactionService.getOneTransaction(id))
        );
    }

    @PostMapping
    public ResponseEntity<TransactionFullDto> createWallet(@Valid @RequestBody TransactionPostRequestDto request) {
        return ResponseEntity.ok(
                dtoMapper.transactionToTransactionFullDto(
                        transactionService.createTransaction(
                                request
                        )
                )
        );
    }

    @PatchMapping
    public ResponseEntity<TransactionFullDto> updateTransaction(@Valid @RequestBody TransactionPatchRequestDto request) {
        return ResponseEntity.ok(
                dtoMapper.transactionToTransactionFullDto(
                        transactionService.updateTransaction(request)
                )
        );
    }

    @DeleteMapping("/{id}")
    public void deleteTransaction(@Valid @PathVariable Long id) {
        transactionService.deleteTransaction(id);
    }

    protected static Sort getSort(String[] sort) {
        if (!((sort[0].equals("id")
                || sort[0].equals("note")
                || sort[0].equals("amount"))
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
