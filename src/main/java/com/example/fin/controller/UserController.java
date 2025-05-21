package com.example.fin.controller;

import com.example.fin.mapper.DtoMapper;
import com.example.fin.mapper.dto.UserFullDto;
import com.example.fin.mapper.dto.UserPatchRequestDto;
import com.example.fin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final DtoMapper dtoMapper;

    @GetMapping
    public ResponseEntity<UserFullDto> getUser() {
        return ResponseEntity.ok(
                dtoMapper.userToUserFullDto(
                        userService.getCurrentUser()
                )
        );
    }

    @PatchMapping
    public ResponseEntity<UserFullDto> updateUser(@RequestBody UserPatchRequestDto request) {
        return ResponseEntity.ok(
                dtoMapper.userToUserFullDto(
                        userService.updateUser(request)
                )
        );
    }

}
