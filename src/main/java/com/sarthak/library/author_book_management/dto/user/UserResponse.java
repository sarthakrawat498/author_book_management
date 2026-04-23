package com.sarthak.library.author_book_management.dto.user;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponse {
    private String username;
    private boolean enabled;
    private String role;
}
