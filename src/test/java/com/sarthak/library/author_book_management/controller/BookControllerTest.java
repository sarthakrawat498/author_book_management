package com.sarthak.library.author_book_management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sarthak.library.author_book_management.dto.book.BookRequest;
import com.sarthak.library.author_book_management.dto.book.BookResponse;
import com.sarthak.library.author_book_management.exception.DuplicateResourceException;
import com.sarthak.library.author_book_management.exception.GlobalExceptionHandler;
import com.sarthak.library.author_book_management.exception.ResourceNotFoundException;
import com.sarthak.library.author_book_management.service.BookService;
import com.sarthak.library.author_book_management.service.BulkService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class BookControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private BookService bookService;

    @Mock
    private BulkService bulkService;

    @InjectMocks
    private BookController bookController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(bookController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private BookRequest buildRequest() {
        BookRequest req = new BookRequest();
        req.setTitle("Clean Code");
        req.setDescription("A handbook of agile software craftsmanship");
        req.setGenre("TECHNOLOGY");
        req.setAuthorId(1L);
        return req;
    }

    private BookResponse buildResponse(Long id) {
        return BookResponse.builder()
                .id(id)
                .title("Clean Code")
                .description("A handbook of agile software craftsmanship")
                .genre("TECHNOLOGY")
                .authorId(1L)
                .authorName("Jane Doe")
                .build();
    }

    // ── POST /api/books ───────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/books: success — 201 with body")
    void createBook_success() throws Exception {
        when(bookService.createBook(any(BookRequest.class))).thenReturn(buildResponse(1L));

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Clean Code"))
                .andExpect(jsonPath("$.authorId").value(1L));

        verify(bookService).createBook(any(BookRequest.class));
    }

    @Test
    @DisplayName("POST /api/books: duplicate book — 409 Conflict")
    void createBook_duplicate_returns409() throws Exception {
        when(bookService.createBook(any(BookRequest.class)))
                .thenThrow(new DuplicateResourceException("Book already exists"));

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("POST /api/books: invalid body — 400 Bad Request")
    void createBook_invalidBody_returns400() throws Exception {
        BookRequest req = new BookRequest(); // all required fields missing

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isMap());
    }

    // ── GET /api/books ────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/books: returns list of books")
    void getAllBooks_returnsList() throws Exception {
        when(bookService.getAllBooks())
                .thenReturn(List.of(buildResponse(1L), buildResponse(2L)));

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
    }

    @Test
    @DisplayName("GET /api/books: empty list")
    void getAllBooks_emptyList() throws Exception {
        when(bookService.getAllBooks()).thenReturn(List.of());

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ── GET /api/books/{id} ───────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/books/{id}: found — 200 with body")
    void getBookById_found() throws Exception {
        when(bookService.getBookById(1L)).thenReturn(buildResponse(1L));

        mockMvc.perform(get("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Clean Code"));
    }

    @Test
    @DisplayName("GET /api/books/{id}: not found — 404")
    void getBookById_notFound() throws Exception {
        when(bookService.getBookById(99L))
                .thenThrow(new ResourceNotFoundException("Book not found with id: 99"));

        mockMvc.perform(get("/api/books/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ── PUT /api/books/{id} ───────────────────────────────────────────────────

    @Test
    @DisplayName("PUT /api/books/{id}: success — 200 with updated body")
    void updateBook_success() throws Exception {
        BookRequest req = buildRequest();
        req.setTitle("Refactoring");
        BookResponse res = BookResponse.builder()
                .id(1L).title("Refactoring").description("A handbook of agile software craftsmanship")
                .genre("TECHNOLOGY").authorId(1L).authorName("Jane Doe").build();

        when(bookService.updateBook(eq(1L), any(BookRequest.class))).thenReturn(res);

        mockMvc.perform(put("/api/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Refactoring"));
    }

    @Test
    @DisplayName("PUT /api/books/{id}: not found — 404")
    void updateBook_notFound() throws Exception {
        when(bookService.updateBook(eq(99L), any(BookRequest.class)))
                .thenThrow(new ResourceNotFoundException("Book not found with id: 99"));

        mockMvc.perform(put("/api/books/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/books/{id}: invalid body — 400")
    void updateBook_invalidBody_returns400() throws Exception {
        BookRequest req = new BookRequest(); // missing required fields

        mockMvc.perform(put("/api/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ── DELETE /api/books/{id} ────────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /api/books/{id}: success — 204 No Content")
    void deleteBook_success() throws Exception {
        doNothing().when(bookService).deleteBook(1L);

        mockMvc.perform(delete("/api/books/1"))
                .andExpect(status().isNoContent());

        verify(bookService).deleteBook(1L);
    }

    @Test
    @DisplayName("DELETE /api/books/{id}: not found — 404")
    void deleteBook_notFound() throws Exception {
        doThrow(new ResourceNotFoundException("Book not found with id: 20"))
                .when(bookService).deleteBook(20L);

        mockMvc.perform(delete("/api/books/20"))
                .andExpect(status().isNotFound());
    }

    // ── POST /api/books/bulk ──────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/books/bulk: success — 201")
    void createBooksBulk_success() throws Exception {
        List<BookRequest> requests = List.of(buildRequest(), buildRequest());
        doNothing().when(bookService).createBooksBulk(any());

        mockMvc.perform(post("/api/books/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isCreated());

        verify(bookService).createBooksBulk(any());
    }

    @Test
    @DisplayName("POST /api/books/bulk: empty list — 400")
    void createBooksBulk_emptyList_returns400() throws Exception {
        mockMvc.perform(post("/api/books/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isBadRequest());
    }
}



