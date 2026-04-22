package com.sarthak.library.author_book_management.repository;

import com.sarthak.library.author_book_management.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.stream.Stream;

public interface BookRepository extends JpaRepository<Book,Long> {

    @Query("SELECT b FROM Book b")
    Stream<Book> streamAllBooks();
}
