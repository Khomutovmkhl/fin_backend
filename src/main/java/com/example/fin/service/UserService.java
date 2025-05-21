package com.example.fin.service;

import com.example.fin.domain.User;
import com.example.fin.mapper.dto.UserPatchRequestDto;

public interface UserService {
    User getCurrentUser();

    User updateUser(UserPatchRequestDto request); // PATCH

}
