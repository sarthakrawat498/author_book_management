package com.sarthak.library.author_book_management.mapper;

import com.sarthak.library.author_book_management.dto.author.AuthorRequest;
import com.sarthak.library.author_book_management.dto.author.AuthorResponse;
import com.sarthak.library.author_book_management.entity.Author;

public class AuthorMapper {
    public static Author toEntity(AuthorRequest request){
        return Author.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .bio(request.getBio())
        .build();
    }
    public static AuthorResponse toResponse(Author author){
        return AuthorResponse.builder()
                .id(author.getId())
                .firstName(author.getFirstName())
                .lastName(author.getLastName())
                .email(author.getEmail())
                .bio(author.getBio())
                .build();
    }
}
