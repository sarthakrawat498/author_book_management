package com.sarthak.library.author_book_management.service;

import com.sarthak.library.author_book_management.dto.book.BookRequest;
import com.sarthak.library.author_book_management.dto.book.BookResponse;

import java.util.List;

public interface BookService {
    BookResponse createBook(BookRequest request);
    BookResponse getBookById(Long id);
    List<BookResponse> getAllBooks();
    BookResponse updateBook(Long id, BookRequest request);
    void deleteBook(Long id);
    void createBooksBulk(List<BookRequest> requests);
}
