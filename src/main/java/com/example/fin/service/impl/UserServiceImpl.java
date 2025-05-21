package com.example.fin.service.impl;

import com.example.fin.auth.AuthenticationService;
import com.example.fin.dao.UserDao;
import com.example.fin.domain.User;
import com.example.fin.mapper.dto.UserPatchRequestDto;
import com.example.fin.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final AuthenticationService authService;

    @Override
    public User getCurrentUser() {
        String email = authService.getAuthentication().getName();
        return userDao.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Cannot identify currently logged-in user: " + email));
    }

    @Override
    public User updateUser(UserPatchRequestDto request) {
        var user = getCurrentUser();
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }

        return userDao.save(user);
    }

}
