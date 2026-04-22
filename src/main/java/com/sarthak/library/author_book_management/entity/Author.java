package com.sarthak.library.author_book_management.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name="authors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="first_name", nullable = false)
    private String firstName;

    @Column(name="last_name")
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(length = 1000)
    private String bio;

    @Column(name="created_at")
    private LocalDateTime createdAt;

    @Column(name= "updated_at")
    private LocalDateTime updatedAt;

    @JsonIgnore //prevents infinite JSON loops : Authors -> Books -> Authors -> Books ...
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Book> books;

    @PrePersist
    public void prePersist(){
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate(){
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Author{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", bio='" + bio + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", books=" + books +
                '}';
    }
}
