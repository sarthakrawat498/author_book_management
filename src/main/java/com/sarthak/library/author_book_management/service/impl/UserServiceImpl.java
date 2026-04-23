package com.sarthak.library.author_book_management.service.impl;

import com.sarthak.library.author_book_management.dto.user.CreateUserRequest;
import com.sarthak.library.author_book_management.dto.user.UserResponse;
import com.sarthak.library.author_book_management.entity.Authority;
import com.sarthak.library.author_book_management.entity.User;
import com.sarthak.library.author_book_management.repository.AuthorityRepository;
import com.sarthak.library.author_book_management.repository.UserRepository;
import com.sarthak.library.author_book_management.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;
    private final PasswordEncoder passwordEncoder;


    @Override
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();

        return users.stream().map(user -> {

            List<Authority> authorities = authorityRepository.findByUsername(user.getUsername());

            String role = authorities.isEmpty() ? null :
                    authorities.get(0).getAuthority().replace("ROLE_", "");

            UserResponse response = new UserResponse();
            response.setUsername(user.getUsername());
            response.setEnabled(user.isEnabled());
            response.setRole(role);

            return response;

        }).toList();
    }

    @Override
    public void createUser(CreateUserRequest request) {
        if (userRepository.existsById(request.getUsername())) {
            throw new RuntimeException("User already exists");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(true);

        userRepository.save(user);

        Authority authority = new Authority();
        authority.setUsername(request.getUsername());
        authority.setAuthority("ROLE_" + request.getRole());

        authorityRepository.save(authority);

    }
    @Override
    public void updateRole(String username, String role) {
        if (!userRepository.existsById(username)) {
            throw new RuntimeException("User not found");
        }

        authorityRepository.deleteByUsername(username);

        Authority authority = new Authority();
        authority.setUsername(username);
        authority.setAuthority("ROLE_" + role);

        authorityRepository.save(authority);
    }
}
