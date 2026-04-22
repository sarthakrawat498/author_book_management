package com.sarthak.library.author_book_management.service.impl;

import com.sarthak.library.author_book_management.enums.Genre;
import com.sarthak.library.author_book_management.dto.book.BookRequest;
import com.sarthak.library.author_book_management.dto.book.BookResponse;
import com.sarthak.library.author_book_management.entity.Author;
import com.sarthak.library.author_book_management.entity.Book;
import com.sarthak.library.author_book_management.exception.ResourceNotFoundException;
import com.sarthak.library.author_book_management.mapper.BookMapper;
import com.sarthak.library.author_book_management.repository.AuthorRepository;
import com.sarthak.library.author_book_management.repository.BookRepository;
import com.sarthak.library.author_book_management.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;

    @Override
    public BookResponse createBook(BookRequest request) {
        Author author = authorRepository.findById(request.getAuthorId())
                .orElseThrow(()-> new ResourceNotFoundException("Author not found"));
        Book book = Book.builder()
                                .title(request.getTitle())
                                .description(request.getDescription())
                                .genre(Genre.valueOf(request.getGenre().toUpperCase()))
                                .author(author)
                                .build();
        Book saved = bookRepository.save(book);
        return BookMapper.toResponse(saved);
    }

    @Override
    public BookResponse getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Book not found with id : " + id ));
        return BookMapper.toResponse(book);

    }

    @Override
    public List<BookResponse> getAllBooks() {
        return bookRepository.findAll()
                .stream()
                .map(book -> BookMapper.toResponse(book))
                .toList();
    }

    @Override
    public BookResponse updateBook(Long id, BookRequest request) {
        Book existingBook = bookRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Book not found with id : " + id ));
        Author author = authorRepository.findById(request.getAuthorId())
                .orElseThrow(()->new ResourceNotFoundException("Author not found"));

        existingBook.setTitle(request.getTitle());
        existingBook.setAuthor(author);
        existingBook.setDescription(request.getDescription());
        existingBook.setGenre(Genre.valueOf(request.getGenre().toUpperCase()));

        Book updatedBook = bookRepository.save(existingBook);
        return BookMapper.toResponse(updatedBook);
    }

    @Override
    public void deleteBook(Long id) {
        if(!bookRepository.existsById(id)){
            throw new ResourceNotFoundException("Book not found with id : " + id );
        }
        bookRepository.deleteById(id);
    }

    @Override
    public void createBooksBulk(List<BookRequest> requests) {
        List<Book> books = new ArrayList<>();
        for(BookRequest request : requests){
            Author author = authorRepository.findById(request.getAuthorId())
                    .orElseThrow(()-> new ResourceNotFoundException("Author not found"));
            Book book = Book.builder()
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .genre(Genre.valueOf(request.getGenre().toUpperCase()))
                    .author(author)
                    .build();
            books.add(book);
        }
        bookRepository.saveAll(books);
    }


}
