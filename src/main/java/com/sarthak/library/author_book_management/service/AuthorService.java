package com.sarthak.library.author_book_management.service;

import com.sarthak.library.author_book_management.dto.author.AuthorRequest;
import com.sarthak.library.author_book_management.dto.author.AuthorResponse;

import java.util.List;

public interface AuthorService {
    AuthorResponse createAuthor(AuthorRequest request);
    List<AuthorResponse> getAllAuthors();
    AuthorResponse getAuthorById(Long id);
    AuthorResponse updateAuthor(Long id, AuthorRequest request);
    void deleteAuthor(Long id);
}
