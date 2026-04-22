package com.sarthak.library.author_book_management.dto.book;

import io.swagger.v3.oas.annotations.info.Info;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookResponse {
    private Long id;
    private String title;
    private String description;
    private String genre;

    private Long authorId;
    private String authorName;

}
