package com.sarthak.library.author_book_management.repository;

import com.sarthak.library.author_book_management.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.stream.Stream;

public interface AuthorRepository extends JpaRepository<Author,Long> {
    Optional<Author> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    @Query("SELECT a FROM Author a")
    Stream<Author> streamAllAuthors();
}
