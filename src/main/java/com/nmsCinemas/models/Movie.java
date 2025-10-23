package com.nmsCinemas.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "movies")
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idmovies")
    private Long idmovies;

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must be ≤ 100 characters")
    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @NotBlank(message = "Genre is required")
    @Size(max = 45, message = "Genre must be ≤ 45 characters")
    @Column(name = "genre", nullable = false, length = 45)
    private String genre;

    @NotBlank(message = "Language is required")
    @Size(max = 45, message = "Language must be ≤ 45 characters")
    @Column(name = "language", nullable = false, length = 45)
    private String language;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be ≥ 1 minute")
    @Column(name = "duration", nullable = false)
    private Integer duration;

    @DecimalMin(value = "0.0", message = "Rating must be ≥ 0.0")
    @DecimalMax(value = "10.0", message = "Rating must be ≤ 10.0")
    @Digits(integer = 2, fraction = 1, message = "Rating must be in format X.X (e.g., 8.5)")
    @Column(name = "rating", precision = 3, scale = 1)
    private BigDecimal rating;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    // ✅ CRITICAL FIX: Add updatable = false
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Constructors
    public Movie() {}

    public Movie(String title, String genre, String language, Integer duration,
                 BigDecimal rating, String description, LocalDate releaseDate) {
        this.title = title;
        this.genre = genre;
        this.language = language;
        this.duration = duration;
        this.rating = rating;
        this.description = description;
        this.releaseDate = releaseDate;
    }

    // ✅ Lifecycle hook to set createdAt automatically
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    // Getters and Setters
    public Long getIdmovies() { return idmovies; }
    public void setIdmovies(Long idmovies) { this.idmovies = idmovies; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

    public BigDecimal getRating() { return rating; }
    public void setRating(BigDecimal rating) { this.rating = rating; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getReleaseDate() { return releaseDate; }
    public void setReleaseDate(LocalDate releaseDate) { this.releaseDate = releaseDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}