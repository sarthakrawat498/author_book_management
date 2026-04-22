package com.sarthak.library.author_book_management.repository;

import com.sarthak.library.author_book_management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,String> {
    Optional<User> findByUsername(String username);
}
