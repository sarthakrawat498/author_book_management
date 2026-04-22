package com.sarthak.library.author_book_management.entity;

import com.sarthak.library.author_book_management.enums.Genre;
import jakarta.persistence.*;

import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name= "books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name= "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name= "author_id", nullable = false)
    private Author author;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Genre genre;

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
        return "Book{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", author=" + author +
                ", description='" + description + '\'' +
                ", genre=" + genre +
                '}';
    }
}
