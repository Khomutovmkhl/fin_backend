package com.example.fin.mapper.dto;

import com.example.fin.domain.TransactionType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionCategoryPostRequestDto {
    @NotNull
    private String name;
    @NotNull
    private TransactionType transactionType;
}
