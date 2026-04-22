package com.sarthak.library.author_book_management.service;

import com.sarthak.library.author_book_management.entity.User;
import com.sarthak.library.author_book_management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
       User user = userRepository.findByUsername(username).orElseThrow(()-> new UsernameNotFoundException("User not found"));
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(),
                true,
                true,
                true,
                user.getAuthorities()
                        .stream()
                        .map(auth-> new SimpleGrantedAuthority(auth.getAuthority()))
                        .collect(Collectors.toList())
        );
    }
}
