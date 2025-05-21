package com.example.fin.controller;

import com.example.fin.mapper.DtoMapper;
import com.example.fin.mapper.dto.*;
import com.example.fin.service.TransactionCategoryService;
import com.example.fin.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class TransactionCategoryController {

    private final TransactionCategoryService transactionCategoryService;
    private final TransactionService transactionService;
    private final DtoMapper dtoMapper;

    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<TransactionFullDto>> getAllTransactionsFromOneCategory(@Valid @PathVariable("id") Long categoryId,
                                                                                      @RequestParam(defaultValue = "id,asc") String[] sort) {
        Sort sorting = TransactionController.getSort(sort);
        return ResponseEntity.ok(
                transactionService.getAllTransactionsFromCategory(categoryId, sorting)
                        .stream()
                        .map(dtoMapper::transactionToTransactionFullDto)
                        .toList()
        );
    }

    @GetMapping
    public ResponseEntity<List<TransactionCategoryFullDto>> getAllCategories(@RequestParam(defaultValue = "id,asc") String[] sort) {
        Sort sorting = getSort(sort);
        return ResponseEntity.ok(
                transactionCategoryService.getAllTransactionCategories(sorting)
                        .stream()
                        .map(dtoMapper::transactionCategoryToTransactionCategoryFullDto)
                        .toList()
        );
    }
    @GetMapping("/{id}")
    public ResponseEntity<TransactionCategoryFullDto> getOneCategory(@Valid @PathVariable("id") Long categoryId) {
        return ResponseEntity.ok(
                dtoMapper.transactionCategoryToTransactionCategoryFullDto(
                        transactionCategoryService.getOneTransactionCategory(categoryId)
                )
        );
    }

    @PostMapping
    public ResponseEntity<TransactionCategoryFullDto> createTransactionCategory(@Valid @RequestBody TransactionCategoryPostRequestDto request) {
        return ResponseEntity.ok(
                dtoMapper.transactionCategoryToTransactionCategoryFullDto(
                        transactionCategoryService.createTransactionCategory(
                                dtoMapper.transactionPostRequestDtoToTransactionCategory(request)
                        )
                )
        );
    }

    @PatchMapping
    public ResponseEntity<TransactionCategoryFullDto> updateTransactionCategory(@Valid @RequestBody TransactionCategoryPatchRequestDto request) {
        return ResponseEntity.ok(
                dtoMapper.transactionCategoryToTransactionCategoryFullDto(
                        transactionCategoryService.updateTransactionCategory(request)
                )
        );
    }

    @DeleteMapping("/{id}")
    public void deleteCategory(@Valid @PathVariable Long id) {
        transactionCategoryService.deleteTransactionCategory(id);
    }

    private static Sort getSort(String[] sort) {
        if (!((sort[0].equals("id")
                || sort[0].equals("name"))
                &&
                (sort[1].equals("desc")
                || sort[1].equals("asc")))) {
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
