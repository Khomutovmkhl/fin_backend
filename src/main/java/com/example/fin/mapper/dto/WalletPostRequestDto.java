package com.example.fin.mapper.dto;

import com.example.fin.domain.Currency;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WalletPostRequestDto {
    @NotNull
    private String name;
    @NotNull
    private BigDecimal deposit;
    @NotNull
    private Currency currency;
}
