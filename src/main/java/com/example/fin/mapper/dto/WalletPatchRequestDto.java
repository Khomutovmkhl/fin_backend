package com.example.fin.mapper.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WalletPatchRequestDto {
    @NotNull
    private Long id;
    private String name;
    private BigDecimal deposit;
}
