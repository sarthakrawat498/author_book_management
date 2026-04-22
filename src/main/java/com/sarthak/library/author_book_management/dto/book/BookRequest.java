package com.sarthak.library.author_book_management.dto.book;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BookRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 2, max = 50)
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Genre is required")
    private String genre;

    @NotNull(message = "Author ID is required")
    private Long authorId;
}
