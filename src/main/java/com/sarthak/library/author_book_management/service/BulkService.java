package com.sarthak.library.author_book_management.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.Writer;

public interface BulkService {
    void importBooksFromCsv(MultipartFile file);
    void importAuthorsFromCsv(MultipartFile file);

    void streamBooksToCsv(Writer writer);
    void streamAuthorsToCsv(Writer writer);
}
