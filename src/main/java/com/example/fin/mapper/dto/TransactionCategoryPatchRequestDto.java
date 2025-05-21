package com.example.fin.mapper.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionCategoryPatchRequestDto {
    @NotNull
    private Long id;
    private String name;
}
