package com.sarthak.library.author_book_management.controller;

import com.sarthak.library.author_book_management.dto.user.CreateUserRequest;
import com.sarthak.library.author_book_management.dto.user.UpdateRoleRequest;
import com.sarthak.library.author_book_management.dto.user.UserResponse;
import com.sarthak.library.author_book_management.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public List<UserResponse> getAllUsers(){
        return userService.getAllUsers();
    }

    @PostMapping
    public void createUser(@Valid @RequestBody CreateUserRequest request) {
        userService.createUser(request);
    }

    @PutMapping("/{username}/role")
    public void updateRole(@PathVariable String username,
                           @Valid @RequestBody UpdateRoleRequest request) {
        userService.updateRole(username, request.getRole());
    }
}
