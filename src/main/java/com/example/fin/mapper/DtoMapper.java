package com.example.fin.mapper;

import com.example.fin.domain.Transaction;
import com.example.fin.domain.TransactionCategory;
import com.example.fin.domain.User;
import com.example.fin.domain.Wallet;
import com.example.fin.mapper.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DtoMapper {
    WalletFullDto walletToWalletFullDto(Wallet wallet);
    Wallet walletPostRequestDtoToWallet(WalletPostRequestDto request);
    @Mapping(target = "transactionCategoryId", source = "transaction.transactionCategory.id")
    @Mapping(target = "transactionCategoryName", source = "transaction.transactionCategory.name")
    TransactionFullDto transactionToTransactionFullDto(Transaction transaction);
    TransactionCategoryFullDto transactionCategoryToTransactionCategoryFullDto(TransactionCategory transactionCategory);
    TransactionCategory transactionPostRequestDtoToTransactionCategory(TransactionCategoryPostRequestDto request);
    UserFullDto userToUserFullDto(User user);
}
