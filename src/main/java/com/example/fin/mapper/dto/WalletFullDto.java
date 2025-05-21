package com.example.fin.mapper.dto;

import com.example.fin.domain.Currency;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WalletFullDto {
    private Long id;
    private String name;
    private BigDecimal balance;
    private BigDecimal deposit;
    private LocalDate createdAt;
    private Currency currency;
}
