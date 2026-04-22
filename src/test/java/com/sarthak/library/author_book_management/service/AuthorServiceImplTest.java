package com.sarthak.library.author_book_management.service;

import com.sarthak.library.author_book_management.dto.author.AuthorRequest;
import com.sarthak.library.author_book_management.dto.author.AuthorResponse;
import com.sarthak.library.author_book_management.entity.Author;
import com.sarthak.library.author_book_management.exception.DuplicateResourceException;
import com.sarthak.library.author_book_management.exception.ResourceNotFoundException;
import com.sarthak.library.author_book_management.repository.AuthorRepository;
import com.sarthak.library.author_book_management.service.impl.AuthorServiceImpl;
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
class AuthorServiceImplTest {

    @Mock
    private AuthorRepository authorRepository;

    @InjectMocks
    private AuthorServiceImpl authorService;

    // ── helpers ──────────────────────────────────────────────────────────────

    private AuthorRequest buildRequest(String email) {
        AuthorRequest req = new AuthorRequest();
        req.setFirstName("Jane");
        req.setLastName("Doe");
        req.setEmail(email);
        req.setBio("Test bio");
        return req;
    }

    private Author buildAuthor(Long id, String email) {
        return Author.builder()
                .id(id)
                .firstName("Jane")
                .lastName("Doe")
                .email(email)
                .bio("Test bio")
                .build();
    }

    // ── createAuthor ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("createAuthor: success — persists and returns response")
    void createAuthor_success() {
        AuthorRequest req = buildRequest("jane@doe.com");
        Author saved = buildAuthor(1L, "jane@doe.com");

        when(authorRepository.existsByEmailIgnoreCase("jane@doe.com")).thenReturn(false);
        when(authorRepository.save(any(Author.class))).thenReturn(saved);

        AuthorResponse response = authorService.createAuthor(req);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getFirstName()).isEqualTo("Jane");
        assertThat(response.getEmail()).isEqualTo("jane@doe.com");

        verify(authorRepository).save(any(Author.class));
    }

    @Test
    @DisplayName("createAuthor: duplicate email — throws DuplicateResourceException")
    void createAuthor_duplicateEmail_throwsException() {
        AuthorRequest req = buildRequest("jane@doe.com");
        when(authorRepository.existsByEmailIgnoreCase("jane@doe.com")).thenReturn(true);

        assertThatThrownBy(() -> authorService.createAuthor(req))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");

        verify(authorRepository, never()).save(any());
    }

    // ── getAllAuthors ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllAuthors: returns mapped list")
    void getAllAuthors_returnsList() {
        Author a1 = buildAuthor(1L, "a1@test.com");
        Author a2 = buildAuthor(2L, "a2@test.com");
        when(authorRepository.findAll()).thenReturn(List.of(a1, a2));

        List<AuthorResponse> result = authorService.getAllAuthors();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(1).getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("getAllAuthors: empty DB — returns empty list")
    void getAllAuthors_emptyList() {
        when(authorRepository.findAll()).thenReturn(List.of());
        assertThat(authorService.getAllAuthors()).isEmpty();
    }

    // ── getAuthorById ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAuthorById: found — returns response")
    void getAuthorById_found() {
        Author author = buildAuthor(5L, "find@me.com");
        when(authorRepository.findById(5L)).thenReturn(Optional.of(author));

        AuthorResponse response = authorService.getAuthorById(5L);

        assertThat(response.getId()).isEqualTo(5L);
        assertThat(response.getEmail()).isEqualTo("find@me.com");
    }

    @Test
    @DisplayName("getAuthorById: not found — throws ResourceNotFoundException")
    void getAuthorById_notFound() {
        when(authorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authorService.getAuthorById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ── updateAuthor ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateAuthor: success — updates all fields and returns response")
    void updateAuthor_success() {
        Author existing = buildAuthor(3L, "old@email.com");
        when(authorRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(authorRepository.save(any(Author.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthorRequest updateReq = buildRequest("new@email.com");
        updateReq.setFirstName("Updated");

        AuthorResponse response = authorService.updateAuthor(3L, updateReq);

        assertThat(response.getFirstName()).isEqualTo("Updated");
        assertThat(response.getEmail()).isEqualTo("new@email.com");
        verify(authorRepository).save(existing);
    }

    @Test
    @DisplayName("updateAuthor: author not found — throws ResourceNotFoundException")
    void updateAuthor_notFound() {
        when(authorRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authorService.updateAuthor(10L, buildRequest("x@x.com")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("10");
    }

    // ── deleteAuthor ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteAuthor: success — calls deleteById")
    void deleteAuthor_success() {
        when(authorRepository.existsById(1L)).thenReturn(true);

        authorService.deleteAuthor(1L);

        verify(authorRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deleteAuthor: not found — throws ResourceNotFoundException, no delete called")
    void deleteAuthor_notFound() {
        when(authorRepository.existsById(20L)).thenReturn(false);

        assertThatThrownBy(() -> authorService.deleteAuthor(20L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("20");

        verify(authorRepository, never()).deleteById(any());
    }
}

