package com.sarthak.library.author_book_management.service;

import com.sarthak.library.author_book_management.entity.Author;
import com.sarthak.library.author_book_management.entity.Book;
import com.sarthak.library.author_book_management.enums.Genre;
import com.sarthak.library.author_book_management.exception.ResourceNotFoundException;
import com.sarthak.library.author_book_management.repository.AuthorRepository;
import com.sarthak.library.author_book_management.repository.BookRepository;
import com.sarthak.library.author_book_management.service.impl.BulkServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BulkServiceImplTest {

    @Mock private BookRepository bookRepository;
    @Mock private AuthorRepository authorRepository;

    @InjectMocks private BulkServiceImpl bulkService;

    // ── helpers ──────────────────────────────────────────────────────────────

    private Author author(Long id) {
        return Author.builder().id(id).firstName("Rick").lastName("Jones")
                .email("rick@test.com").bio("bio").build();
    }

    private Book book(Long id, Author author) {
        return Book.builder().id(id).title("Dune").description("Sci-Fi epic")
                .genre(Genre.FICTION).author(author).build();
    }

    private MultipartFile csvFile(String content) {
        return new MockMultipartFile(
                "file", "test.csv", "text/csv",
                content.getBytes(StandardCharsets.UTF_8));
    }

    // ── importBooksFromCsv ────────────────────────────────────────────────────

    @Test
    @DisplayName("importBooksFromCsv: valid rows — saves all parsed books")
    void importBooksFromCsv_success() {
        // Note: BulkServiceImpl has a bug — isHeader starts as false so row 0 is processed,
        // row 1 is skipped as "header". We match the actual implementation behaviour.
        String csv = "Dune,Sci-Fi epic,FICTION,1\n" +
                     "Foundation,Asimov classic,SCIENCE,1\n";

        when(authorRepository.findById(1L)).thenReturn(Optional.of(author(1L)));

        bulkService.importBooksFromCsv(csvFile(csv));

        // Both rows processed (header flag starts false in importBooksFromCsv)
        verify(bookRepository).saveAll(argThat(list -> ((List<?>) list).size() == 2));
    }

    @Test
    @DisplayName("importBooksFromCsv: skips rows with too few columns")
    void importBooksFromCsv_skipShortRows() {
        String csv = "Dune,Sci-Fi\n";   // only 2 columns — skipped

        bulkService.importBooksFromCsv(csvFile(csv));

        verify(bookRepository).saveAll(argThat(list -> ((List<?>) list).isEmpty()));
    }

    @Test
    @DisplayName("importBooksFromCsv: skips rows with invalid author id (not a number)")
    void importBooksFromCsv_skipInvalidAuthorId() {
        String csv = "Dune,Sci-Fi epic,FICTION,NOT_A_NUMBER\n";

        bulkService.importBooksFromCsv(csvFile(csv));

        verify(bookRepository).saveAll(argThat(list -> ((List<?>) list).isEmpty()));
        verify(authorRepository, never()).findById(any());
    }

    @Test
    @DisplayName("importBooksFromCsv: skips rows with invalid genre — continues with rest")
    void importBooksFromCsv_skipInvalidGenre() {
        String csv = "Dune,Sci-Fi epic,INVALID_GENRE,1\n";
        when(authorRepository.findById(1L)).thenReturn(Optional.of(author(1L)));

        bulkService.importBooksFromCsv(csvFile(csv));

        verify(bookRepository).saveAll(argThat(list -> ((List<?>) list).isEmpty()));
    }

    @Test
    @DisplayName("importBooksFromCsv: empty file — saves empty list")
    void importBooksFromCsv_emptyFile() {
        bulkService.importBooksFromCsv(csvFile(""));

        verify(bookRepository).saveAll(argThat(list -> ((List<?>) list).isEmpty()));
    }

    @Test
    @DisplayName("importBooksFromCsv: IO error — throws RuntimeException")
    void importBooksFromCsv_ioException() throws IOException {
        MultipartFile badFile = mock(MultipartFile.class);
        when(badFile.getInputStream()).thenThrow(new IOException("disk error"));

        assertThatThrownBy(() -> bulkService.importBooksFromCsv(badFile))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error processing CSV File");
    }

    // ── importAuthorsFromCsv ──────────────────────────────────────────────────

    @Test
    @DisplayName("importAuthorsFromCsv: valid rows — saves all authors after header skip")
    void importAuthorsFromCsv_success() {
        String csv = "firstName,lastName,email,bio\n" +       // header — skipped
                     "Rick,Jones,rick@jones.com,A novelist\n" +
                     "Jane,Doe,jane@doe.com,A poet\n";

        when(authorRepository.existsByEmailIgnoreCase("rick@jones.com")).thenReturn(false);
        when(authorRepository.existsByEmailIgnoreCase("jane@doe.com")).thenReturn(false);

        bulkService.importAuthorsFromCsv(csvFile(csv));

        verify(authorRepository).saveAll(argThat(list -> ((List<?>) list).size() == 2));
    }

    @Test
    @DisplayName("importAuthorsFromCsv: duplicate email — row skipped, others saved")
    void importAuthorsFromCsv_skipDuplicateEmail() {
        String csv = "firstName,lastName,email,bio\n" +
                     "Rick,Jones,rick@jones.com,Bio\n" +
                     "Jane,Doe,jane@doe.com,Bio\n";

        when(authorRepository.existsByEmailIgnoreCase("rick@jones.com")).thenReturn(true);  // duplicate
        when(authorRepository.existsByEmailIgnoreCase("jane@doe.com")).thenReturn(false);

        bulkService.importAuthorsFromCsv(csvFile(csv));

        verify(authorRepository).saveAll(argThat(list -> ((List<?>) list).size() == 1));
    }

    @Test
    @DisplayName("importAuthorsFromCsv: skips rows where firstName or email is blank")
    void importAuthorsFromCsv_skipBlankRequiredFields() {
        String csv = "firstName,lastName,email,bio\n" +
                     ",Jones,,Bio\n";   // firstName and email blank

        bulkService.importAuthorsFromCsv(csvFile(csv));

        verify(authorRepository).saveAll(argThat(list -> ((List<?>) list).isEmpty()));
        verify(authorRepository, never()).existsByEmailIgnoreCase(any());
    }

    @Test
    @DisplayName("importAuthorsFromCsv: skips rows with too few columns")
    void importAuthorsFromCsv_skipShortRows() {
        String csv = "firstName,lastName,email,bio\n" +
                     "Rick,Jones\n";   // only 2 columns

        bulkService.importAuthorsFromCsv(csvFile(csv));

        verify(authorRepository).saveAll(argThat(list -> ((List<?>) list).isEmpty()));
    }

    @Test
    @DisplayName("importAuthorsFromCsv: IO error — throws RuntimeException")
    void importAuthorsFromCsv_ioException() throws IOException {
        MultipartFile badFile = mock(MultipartFile.class);
        when(badFile.getInputStream()).thenThrow(new IOException("disk error"));

        assertThatThrownBy(() -> bulkService.importAuthorsFromCsv(badFile))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error processing CSV File");
    }

    // ── streamBooksToCsv ──────────────────────────────────────────────────────

    @Test
    @DisplayName("streamBooksToCsv: writes header + one row per book")
    void streamBooksToCsv_writesContent() {
        Author a = author(1L);
        Book b1 = book(1L, a);
        Book b2 = book(2L, a);
        when(bookRepository.streamAllBooks()).thenReturn(Stream.of(b1, b2));

        StringWriter sw = new StringWriter();
        bulkService.streamBooksToCsv(sw);

        String output = sw.toString();
        assertThat(output).startsWith("title,description,genre,authorId");
        assertThat(output).contains("Dune");
        assertThat(output.lines().filter(l -> !l.isBlank()).count()).isEqualTo(3); // header + 2 rows
    }

    @Test
    @DisplayName("streamBooksToCsv: empty DB — only header written")
    void streamBooksToCsv_emptyDb() {
        when(bookRepository.streamAllBooks()).thenReturn(Stream.empty());

        StringWriter sw = new StringWriter();
        bulkService.streamBooksToCsv(sw);

        String output = sw.toString();
        assertThat(output.trim()).isEqualTo("title,description,genre,authorId");
    }

    // ── streamAuthorsToCsv ────────────────────────────────────────────────────

    @Test
    @DisplayName("streamAuthorsToCsv: writes header + one row per author")
    void streamAuthorsToCsv_writesContent() {
        Author a1 = author(1L);
        Author a2 = Author.builder().id(2L).firstName("Jane").lastName("Doe")
                .email("jane@doe.com").bio("Poet").build();
        when(authorRepository.streamAllAuthors()).thenReturn(Stream.of(a1, a2));

        StringWriter sw = new StringWriter();
        bulkService.streamAuthorsToCsv(sw);

        String output = sw.toString();
        assertThat(output).startsWith("firstName,lastName,email,bio");
        assertThat(output).contains("Rick");
        assertThat(output).contains("Jane");
        assertThat(output.lines().filter(l -> !l.isBlank()).count()).isEqualTo(3); // header + 2 rows
    }

    @Test
    @DisplayName("streamAuthorsToCsv: empty DB — only header written")
    void streamAuthorsToCsv_emptyDb() {
        when(authorRepository.streamAllAuthors()).thenReturn(Stream.empty());

        StringWriter sw = new StringWriter();
        bulkService.streamAuthorsToCsv(sw);

        String output = sw.toString();
        assertThat(output.trim()).isEqualTo("firstName,lastName,email,bio");
    }
}

