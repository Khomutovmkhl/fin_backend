package com.example.fin.mapper.dto;

import com.example.fin.domain.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionFullDto {
    private Long id;
    private String note;
    private BigDecimal amount;
    private LocalDate createdAt;
    private TransactionType transactionType;
    private Long transactionCategoryId;
    private String transactionCategoryName;
}
