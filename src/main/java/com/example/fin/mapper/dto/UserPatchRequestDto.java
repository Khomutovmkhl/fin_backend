package com.example.fin.mapper.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserPatchRequestDto {
    private String firstName;
    private String lastName;
}
