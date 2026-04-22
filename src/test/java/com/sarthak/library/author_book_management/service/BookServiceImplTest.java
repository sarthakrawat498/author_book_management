package com.sarthak.library.author_book_management.service;

import com.sarthak.library.author_book_management.dto.book.BookRequest;
import com.sarthak.library.author_book_management.dto.book.BookResponse;
import com.sarthak.library.author_book_management.entity.Author;
import com.sarthak.library.author_book_management.entity.Book;
import com.sarthak.library.author_book_management.enums.Genre;
import com.sarthak.library.author_book_management.exception.ResourceNotFoundException;
import com.sarthak.library.author_book_management.repository.AuthorRepository;
import com.sarthak.library.author_book_management.repository.BookRepository;
import com.sarthak.library.author_book_management.service.impl.BookServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock private BookRepository bookRepository;
    @Mock private AuthorRepository authorRepository;

    @InjectMocks private BookServiceImpl bookService;

    // ── helpers ──────────────────────────────────────────────────────────────

    private Author author(Long id) {
        return Author.builder().id(id).firstName("Rick").lastName("Jones").build();
    }

    private Book book(Long id, Author author) {
        return Book.builder().id(id).title("Clean Code").description("Desc")
                .genre(Genre.TECHNOLOGY).author(author).build();
    }

    private BookRequest request(Long authorId) {
        BookRequest r = new BookRequest();
        r.setTitle("Clean Code");
        r.setDescription("Desc");
        r.setGenre("TECHNOLOGY");
        r.setAuthorId(authorId);
        return r;
    }

    // ── createBook ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("createBook: success — returns mapped BookResponse")
    void createBook_success() {
        Author author = author(1L);
        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> {
            Book b = inv.getArgument(0);
            b.setId(10L);
            return b;
        });

        BookResponse response = bookService.createBook(request(1L));

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Clean Code");
        assertThat(response.getAuthorId()).isEqualTo(1L);
        assertThat(response.getGenre()).isEqualTo("TECHNOLOGY");
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    @DisplayName("createBook: author not found — throws ResourceNotFoundException")
    void createBook_authorNotFound() {
        when(authorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.createBook(request(1L)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Author not found");

        verify(bookRepository, never()).save(any());
    }

    @Test
    @DisplayName("createBook: invalid genre string — throws IllegalArgumentException")
    void createBook_invalidGenre() {
        BookRequest req = request(1L);
        req.setGenre("UNKNOWN_GENRE");
        when(authorRepository.findById(1L)).thenReturn(Optional.of(author(1L)));

        assertThatThrownBy(() -> bookService.createBook(req))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ── getBookById ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("getBookById: found — returns response")
    void getBookById_success() {
        Author author = author(1L);
        Book book = book(1L, author);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        BookResponse response = bookService.getBookById(1L);

        assertThat(response.getTitle()).isEqualTo("Clean Code");
        assertThat(response.getAuthorId()).isEqualTo(1L);
        assertThat(response.getGenre()).isEqualTo("TECHNOLOGY");
    }

    @Test
    @DisplayName("getBookById: not found — throws ResourceNotFoundException")
    void getBookById_notFound() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getBookById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ── getAllBooks ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllBooks: returns mapped list of all books")
    void getAllBooks_returnsList() {
        Author author = author(1L);
        when(bookRepository.findAll()).thenReturn(List.of(book(1L, author), book(2L, author)));

        List<BookResponse> result = bookService.getAllBooks();

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("getAllBooks: empty DB — returns empty list")
    void getAllBooks_emptyList() {
        when(bookRepository.findAll()).thenReturn(List.of());
        assertThat(bookService.getAllBooks()).isEmpty();
    }

    // ── updateBook ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateBook: success — updates and returns response")
    void updateBook_success() {
        Author author = author(1L);
        Book existing = book(5L, author);

        when(bookRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        BookRequest updateReq = request(1L);
        updateReq.setTitle("Updated Title");

        BookResponse response = bookService.updateBook(5L, updateReq);

        assertThat(response.getTitle()).isEqualTo("Updated Title");
        verify(bookRepository).save(existing);
    }

    @Test
    @DisplayName("updateBook: book not found — throws ResourceNotFoundException")
    void updateBook_bookNotFound() {
        when(bookRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.updateBook(5L, request(1L)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("5");
    }

    @Test
    @DisplayName("updateBook: author not found — throws ResourceNotFoundException")
    void updateBook_authorNotFound() {
        when(bookRepository.findById(5L)).thenReturn(Optional.of(book(5L, author(1L))));
        when(authorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.updateBook(5L, request(1L)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Author not found");
    }

    // ── deleteBook ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteBook: success — calls deleteById")
    void deleteBook_success() {
        when(bookRepository.existsById(1L)).thenReturn(true);

        bookService.deleteBook(1L);

        verify(bookRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deleteBook: not found — throws ResourceNotFoundException, deleteById never called")
    void deleteBook_notFound() {
        when(bookRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> bookService.deleteBook(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("1");

        verify(bookRepository, never()).deleteById(any());
    }

    // ── createBooksBulk ───────────────────────────────────────────────────────

    @Test
    @DisplayName("createBooksBulk: success — saveAll called once with all books")
    void createBooksBulk_success() {
        Author author = author(1L);
        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));

        bookService.createBooksBulk(List.of(request(1L), request(1L)));

        verify(bookRepository).saveAll(argThat(list -> ((List<?>) list).size() == 2));
    }

    @Test
    @DisplayName("createBooksBulk: one author missing — throws ResourceNotFoundException")
    void createBooksBulk_authorMissing() {
        when(authorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.createBooksBulk(List.of(request(1L))))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
