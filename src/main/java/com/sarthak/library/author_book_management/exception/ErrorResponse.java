package com.sarthak.library.author_book_management.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorResponse {

    private String message;
    private int status;
    private long timestamp;
}
