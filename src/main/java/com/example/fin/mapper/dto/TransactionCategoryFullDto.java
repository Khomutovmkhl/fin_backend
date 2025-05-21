package com.example.fin.mapper.dto;

import com.example.fin.domain.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionCategoryFullDto {
    private Long id;
    private String name;
    private TransactionType transactionType;
    private BigDecimal categoryAmount;
}
