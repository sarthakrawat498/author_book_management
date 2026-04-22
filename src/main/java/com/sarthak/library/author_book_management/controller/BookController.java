package com.sarthak.library.author_book_management.controller;

import com.sarthak.library.author_book_management.dto.book.BookRequest;
import com.sarthak.library.author_book_management.dto.book.BookResponse;
import com.sarthak.library.author_book_management.entity.Book;
import com.sarthak.library.author_book_management.service.BookService;
import com.sarthak.library.author_book_management.service.BulkService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books")
public class BookController {
    private final BookService bookService;
    private final BulkService bulkService;

    //Create
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookResponse createBook(@Valid @RequestBody BookRequest request){
        return bookService.createBook(request);
    }
    //Read
    @GetMapping("/{id}")
    public BookResponse getBookById(@PathVariable Long id){
        return bookService.getBookById(id);
    }
    //Read All
    @GetMapping
    public List<BookResponse> getAllBooks(){
        return bookService.getAllBooks();
    }
    //Update
    @PutMapping("/{id}")
    public BookResponse updateBook(@PathVariable Long id,@Valid @RequestBody BookRequest request){
        return bookService.updateBook(id,request);
    }
    //Delete
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBook(@PathVariable Long id){
        bookService.deleteBook(id);
    }
    //Read CSV Import
    @PostMapping("/import")
    @ResponseStatus(HttpStatus.CREATED)
    public void importBooks(@RequestParam("file")MultipartFile file){
        bulkService.importBooksFromCsv(file);
    }

    //Read JSON Bulk
    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public void createBooksBulk(@RequestBody
            @NotEmpty(message = "Book list cannot be empty")
                                    @Size(max=100 , message = "Max 100 books allowed")
                                    List<@Valid BookRequest> requests){
        bookService.createBooksBulk(requests);
    }

    //export JSON bulk
    @GetMapping("/export")
    public void exportBooks(HttpServletResponse response ) throws IOException{
        response.setContentType("text/csv");
        response.setCharacterEncoding("UTF-8");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=books.csv");

        bulkService.streamBooksToCsv(response.getWriter());
    }
}
