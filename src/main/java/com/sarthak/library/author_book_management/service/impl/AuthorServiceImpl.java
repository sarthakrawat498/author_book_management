package com.sarthak.library.author_book_management.service.impl;

import com.sarthak.library.author_book_management.dto.author.AuthorRequest;
import com.sarthak.library.author_book_management.dto.author.AuthorResponse;
import com.sarthak.library.author_book_management.entity.Author;
import com.sarthak.library.author_book_management.exception.DuplicateResourceException;
import com.sarthak.library.author_book_management.exception.ResourceNotFoundException;
import com.sarthak.library.author_book_management.mapper.AuthorMapper;
import com.sarthak.library.author_book_management.repository.AuthorRepository;
import com.sarthak.library.author_book_management.service.AuthorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthorServiceImpl implements AuthorService {
    private final AuthorRepository authorRepository;
    @Override
    public AuthorResponse createAuthor(AuthorRequest request) {
        if(authorRepository.existsByEmailIgnoreCase(request.getEmail())){
            throw new DuplicateResourceException("Author with this email already exists");
        }
        Author author = AuthorMapper.toEntity(request);
        Author saved = authorRepository.save(author);
        return AuthorMapper.toResponse(saved);
    }

    @Override
    public List<AuthorResponse> getAllAuthors() {
        return authorRepository.findAll()
                .stream()
                .map(author -> AuthorMapper.toResponse(author))
                .toList();
    }

    @Override
    public AuthorResponse getAuthorById(Long id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Author not found with id : " + id));
        return AuthorMapper.toResponse(author);
    }

    @Override
    public AuthorResponse updateAuthor(Long id, AuthorRequest request) {
        Author existingAuthor = authorRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Author not found with id : " + id));

        existingAuthor.setFirstName(request.getFirstName());
        existingAuthor.setLastName(request.getLastName());
        existingAuthor.setEmail(request.getEmail());
        existingAuthor.setBio(request.getBio());

        Author updatedAuthor = authorRepository.save(existingAuthor);
        return AuthorMapper.toResponse(updatedAuthor);
    }

    @Override
    public void deleteAuthor(Long id) {
        if(!authorRepository.existsById(id)){
            throw new ResourceNotFoundException("Author not found with id : " + id);
        }
        authorRepository.deleteById(id);
    }
}
