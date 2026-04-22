package com.sarthak.library.author_book_management.service.impl;

import com.sarthak.library.author_book_management.enums.Genre;
import com.sarthak.library.author_book_management.entity.Author;
import com.sarthak.library.author_book_management.entity.Book;
import com.sarthak.library.author_book_management.exception.ResourceNotFoundException;
import com.sarthak.library.author_book_management.repository.AuthorRepository;
import com.sarthak.library.author_book_management.repository.BookRepository;
import com.sarthak.library.author_book_management.service.BulkService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.sarthak.library.author_book_management.util.CsvUtil.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class BulkServiceImpl implements BulkService {
    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    @Override
    public void importBooksFromCsv(MultipartFile file) {
        try(BufferedReader bfr = new BufferedReader(
                new InputStreamReader(file.getInputStream())
        )){
            boolean isHeader = false;
            String line;
            List<Book> books = new ArrayList<>();
            while((line = bfr.readLine()) != null){
                if(isHeader){
                    isHeader = false;
                    continue;
                }
                if(line.trim().isEmpty())continue;
                String[] data = parseCsvLine(line);
                // Expected CSV format:
                // title,description,genre,authorId

                if(data.length < 4)continue;

                String title = data[0].trim();
                String description = data[1].trim();
                String genStr = data[2].trim();
                String authorIdStr = data[3].trim();
                try{
                    Long authorId = Long.parseLong(authorIdStr);
                    Author author = authorRepository.findById(authorId)
                            .orElseThrow(()-> new ResourceNotFoundException("Author not found"));
                    Genre genre = Genre.valueOf(genStr.toUpperCase());
                    Book book = Book.builder().title(title).description(description).genre(genre).author(author).build();
                    books.add(book);
                }catch(Exception ex){
                    log.warn("Skipping row : {}" , line);
                }
            }
            bookRepository.saveAll(books);
        }catch(IOException e){
            throw new RuntimeException("Error processing CSV File",e);
        }
    }

    @Override
    public void importAuthorsFromCsv(MultipartFile file) {
        try(BufferedReader bfr = new BufferedReader(
                new InputStreamReader(file.getInputStream())
        )){
            boolean isHeader = true;
            String line;
            List<Author> authors = new ArrayList<>();
            while((line = bfr.readLine()) != null){
                if(isHeader){
                    isHeader = false;
                    continue;
                }
                if(line.trim().isEmpty())continue;
                String[] data = parseCsvLine(line);
                // Expected CSV format:
                // firstname,lastname,email,bio

                if(data.length < 4)continue;

                String firstName = data[0].trim();
                String lastName = data[1].trim();
                String email = data[2].trim();
                String bio = data[3].trim();
                if (firstName.isEmpty() || email.isEmpty()) continue;
                try{
                    if(authorRepository.existsByEmailIgnoreCase(email))continue;
                    Author author = Author.builder().firstName(firstName).lastName(lastName).email(email).bio(bio).build();
                    authors.add(author);
                }catch(Exception ex){
                    log.warn("Skipping row : {}" , line);
                }
            }
            authorRepository.saveAll(authors);
        }
        catch (IOException e){
            throw new RuntimeException("Error processing CSV File",e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void streamBooksToCsv(Writer writer) {
        BufferedWriter bw = new BufferedWriter(writer);
        try(Stream<Book> stream = bookRepository.streamAllBooks()){
            bw.write("title,description,genre,authorId");
            bw.newLine();
            stream.forEach(book -> {
                try{
                    bw.write(escapeCSV(book.getTitle()) + "," +
                            escapeCSV(book.getDescription()) + "," +
                            book.getGenre().name() + "," +
                            book.getAuthor().getId());
                    bw.newLine();
                }
                catch (IOException ex){
                    throw new RuntimeException(ex);
                }
            });
            bw.flush();
        }
        catch (IOException ex){
            throw new RuntimeException("Error streaming CSV ", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void streamAuthorsToCsv(Writer writer) {
        BufferedWriter bw = new BufferedWriter(writer);
        try(Stream<Author> stream = authorRepository.streamAllAuthors()){
            bw.write("firstName,lastName,email,bio");
            bw.newLine();
            stream.forEach(author -> {
                try{
                    bw.write(escapeCSV(author.getFirstName()) + "," +
                            escapeCSV(author.getLastName()) + "," +
                            escapeCSV(author.getEmail()) + "," +
                            escapeCSV(author.getBio()));
                    bw.newLine();
                }catch (IOException ex){
                    throw new RuntimeException(ex);
                }
            });
            bw.flush();
        }catch (IOException ex){
            throw new RuntimeException("Error Streaming CSV ", ex);
        }
    }
}

