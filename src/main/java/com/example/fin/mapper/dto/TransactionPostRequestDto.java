package com.example.fin.mapper.dto;

import com.example.fin.domain.TransactionType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionPostRequestDto {
    private String note;
    @NotNull
    private BigDecimal amount;
    @NotNull
    private TransactionType transactionType;
    @NotNull
    private Long transactionCategoryId;
    @NotNull
    private Long walletId;
}
