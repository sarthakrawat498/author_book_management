package com.sarthak.library.author_book_management.service;

import com.sarthak.library.author_book_management.dto.user.CreateUserRequest;
import com.sarthak.library.author_book_management.dto.user.UserResponse;

import java.util.List;

public interface UserService {
    List<UserResponse> getAllUsers();
    void createUser(CreateUserRequest request);
    void updateRole(String username, String role);
}
