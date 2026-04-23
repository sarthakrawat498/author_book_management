package com.sarthak.library.author_book_management.repository;

import com.sarthak.library.author_book_management.entity.Authority;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuthorityRepository extends JpaRepository<Authority, Long> {
    void deleteByUsername(String username);
    List<Authority> findByUsername(String username);
}
