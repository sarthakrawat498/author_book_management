package com.sarthak.library.author_book_management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sarthak.library.author_book_management.dto.author.AuthorRequest;
import com.sarthak.library.author_book_management.dto.author.AuthorResponse;
import com.sarthak.library.author_book_management.exception.DuplicateResourceException;
import com.sarthak.library.author_book_management.exception.GlobalExceptionHandler;
import com.sarthak.library.author_book_management.exception.ResourceNotFoundException;
import com.sarthak.library.author_book_management.service.AuthorService;
import com.sarthak.library.author_book_management.service.BulkService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthorControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AuthorService authorService;

    @Mock
    private BulkService bulkService;

    @InjectMocks
    private AuthorController authorController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(authorController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private AuthorRequest buildRequest() {
        AuthorRequest req = new AuthorRequest();
        req.setFirstName("Jane");
        req.setLastName("Doe");
        req.setEmail("jane@doe.com");
        req.setBio("Test bio");
        return req;
    }

    private AuthorResponse buildResponse(Long id) {
        return AuthorResponse.builder()
                .id(id)
                .firstName("Jane")
                .lastName("Doe")
                .email("jane@doe.com")
                .bio("Test bio")
                .build();
    }

    // ── POST /api/authors ─────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/authors: success — 201 with body")
    void createAuthor_success() throws Exception {
        AuthorRequest req = buildRequest();
        AuthorResponse res = buildResponse(1L);

        when(authorService.createAuthor(any(AuthorRequest.class))).thenReturn(res);

        mockMvc.perform(post("/api/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.email").value("jane@doe.com"));

        verify(authorService).createAuthor(any(AuthorRequest.class));
    }

    @Test
    @DisplayName("POST /api/authors: duplicate email — 409 Conflict")
    void createAuthor_duplicateEmail_returns409() throws Exception {
        AuthorRequest req = buildRequest();

        when(authorService.createAuthor(any(AuthorRequest.class)))
                .thenThrow(new DuplicateResourceException("Author with email already exists"));

        mockMvc.perform(post("/api/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("POST /api/authors: invalid body — 400 Bad Request")
    void createAuthor_invalidBody_returns400() throws Exception {
        AuthorRequest req = new AuthorRequest(); // missing required fields

        mockMvc.perform(post("/api/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isMap());
    }

    // ── GET /api/authors ──────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/authors: returns list of authors")
    void getAllAuthors_returnsList() throws Exception {
        when(authorService.getAllAuthors())
                .thenReturn(List.of(buildResponse(1L), buildResponse(2L)));

        mockMvc.perform(get("/api/authors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
    }

    @Test
    @DisplayName("GET /api/authors: empty list")
    void getAllAuthors_emptyList() throws Exception {
        when(authorService.getAllAuthors()).thenReturn(List.of());

        mockMvc.perform(get("/api/authors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ── GET /api/authors/{id} ─────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/authors/{id}: found — 200 with body")
    void getAuthorById_found() throws Exception {
        when(authorService.getAuthorById(1L)).thenReturn(buildResponse(1L));

        mockMvc.perform(get("/api/authors/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("jane@doe.com"));
    }

    @Test
    @DisplayName("GET /api/authors/{id}: not found — 404")
    void getAuthorById_notFound() throws Exception {
        when(authorService.getAuthorById(99L))
                .thenThrow(new ResourceNotFoundException("Author not found with id: 99"));

        mockMvc.perform(get("/api/authors/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ── PUT /api/authors/{id} ─────────────────────────────────────────────────

    @Test
    @DisplayName("PUT /api/authors/{id}: success — 200 with updated body")
    void updateAuthor_success() throws Exception {
        AuthorRequest req = buildRequest();
        req.setFirstName("Updated");
        AuthorResponse res = AuthorResponse.builder()
                .id(1L).firstName("Updated").lastName("Doe")
                .email("jane@doe.com").bio("Test bio").build();

        when(authorService.updateAuthor(eq(1L), any(AuthorRequest.class))).thenReturn(res);

        mockMvc.perform(put("/api/authors/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"));
    }

    @Test
    @DisplayName("PUT /api/authors/{id}: not found — 404")
    void updateAuthor_notFound() throws Exception {
        when(authorService.updateAuthor(eq(99L), any(AuthorRequest.class)))
                .thenThrow(new ResourceNotFoundException("Author not found with id: 99"));

        mockMvc.perform(put("/api/authors/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/authors/{id}: invalid body — 400")
    void updateAuthor_invalidBody_returns400() throws Exception {
        AuthorRequest req = new AuthorRequest(); // missing required fields

        mockMvc.perform(put("/api/authors/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ── DELETE /api/authors/{id} ──────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /api/authors/{id}: success — 204 No Content")
    void deleteAuthor_success() throws Exception {
        doNothing().when(authorService).deleteAuthor(1L);

        mockMvc.perform(delete("/api/authors/1"))
                .andExpect(status().isNoContent());

        verify(authorService).deleteAuthor(1L);
    }

    @Test
    @DisplayName("DELETE /api/authors/{id}: not found — 404")
    void deleteAuthor_notFound() throws Exception {
        doThrow(new ResourceNotFoundException("Author not found with id: 20"))
                .when(authorService).deleteAuthor(20L);

        mockMvc.perform(delete("/api/authors/20"))
                .andExpect(status().isNotFound());
    }
}



