package com.sarthak.library.author_book_management.mapper;

import com.sarthak.library.author_book_management.dto.book.BookRequest;
import com.sarthak.library.author_book_management.dto.book.BookResponse;
import com.sarthak.library.author_book_management.entity.Book;

public class BookMapper {
    public static BookResponse toResponse(Book book){
        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .description(book.getDescription())
                .genre(book.getGenre().name())
                .authorId(book.getAuthor().getId())
                .authorName(book.getAuthor().getFirstName() + " " + book.getAuthor().getLastName())
                .build();
    }
}
