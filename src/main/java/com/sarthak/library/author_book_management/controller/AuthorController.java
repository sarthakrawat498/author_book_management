package com.sarthak.library.author_book_management.controller;


import com.sarthak.library.author_book_management.dto.author.AuthorRequest;
import com.sarthak.library.author_book_management.dto.author.AuthorResponse;
import com.sarthak.library.author_book_management.entity.Author;
import com.sarthak.library.author_book_management.repository.AuthorRepository;
import com.sarthak.library.author_book_management.repository.BookRepository;
import com.sarthak.library.author_book_management.service.AuthorService;
import com.sarthak.library.author_book_management.service.BulkService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/authors")
@RequiredArgsConstructor
public class AuthorController {
    private final AuthorService authorService;
    private final BulkService bulkService;

    //Create
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AuthorResponse createAuthor(@Valid @RequestBody AuthorRequest request){
        return authorService.createAuthor(request);
    }

    //Read by I'd
    @GetMapping("/{id}")
    public AuthorResponse getAuthorById(@PathVariable Long id){
        return authorService.getAuthorById(id);
    }

    //Read ALL
    @GetMapping
    public List<AuthorResponse> getAllAuthors(){
        return authorService.getAllAuthors();
    }

    //Update (PUT)
    @PutMapping("/{id}")
    public AuthorResponse updateAuthor(@PathVariable Long id ,@Valid @RequestBody AuthorRequest request){
        return authorService.updateAuthor(id, request);
    }

    //Delete
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAuthor(@PathVariable Long id){
        authorService.deleteAuthor(id);
    }

    //CSV import
    @PostMapping("/import")
    @ResponseStatus(HttpStatus.CREATED)
    public void importAuthors(@RequestParam("file")MultipartFile file){
        bulkService.importAuthorsFromCsv(file);
    }

    //CSV Export
    @GetMapping("/export")
    public void exportAuthors(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setCharacterEncoding("UTF-8");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=authors.csv");

        bulkService.streamAuthorsToCsv(response.getWriter());
    }

}
