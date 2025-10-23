package com.nmsCinemas.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "shows")
@IdClass(ShowId.class)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Show {

    // === Composite Primary Key Fields ===
    @Id
    @Column(name = "idshows")
    private Long idshows;

    @Id
    @Column(name = "movies_idmovies", nullable = false)
    private Long moviesIdmovies;

    @Id
    @Column(name = "theatres_idtheatres", nullable = false)
    private Long theatresIdtheatres;

    // === Columns ===
    @NotNull(message = "Show date is required.")
    @Column(name = "show_date", nullable = false)
    private LocalDate showDate;

    @NotNull(message = "Show time is required.")
    @Column(name = "show_time", nullable = false)
    private LocalTime showTime;

    @NotNull(message = "Available seats are required.")
    @Min(value = 0, message = "Available seats cannot be negative.")
    @Column(name = "available_seats", nullable = false)
    private Integer availableSeats;

    @NotNull(message = "Price is required.")
    @DecimalMin(value = "0.00", inclusive = true, message = "Price must be >= 0.00.")
    @Digits(integer = 8, fraction = 2, message = "Price must be a valid amount with up to 2 decimals.")
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // === Relationships (read-only joins for display) ===
    // ✅ CRITICAL: @JsonIgnore prevents lazy loading errors in REST API
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movies_idmovies", referencedColumnName = "idmovies",
                insertable = false, updatable = false)
    private Movie movie;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theatres_idtheatres", referencedColumnName = "idtheatres",
                insertable = false, updatable = false)
    private Theatre theatre;

    // === Constructors ===
    public Show() {}

    public Show(Long idshows, Long moviesIdmovies, Long theatresIdtheatres,
                LocalDate showDate, LocalTime showTime, Integer availableSeats,
                BigDecimal price, LocalDateTime createdAt) {
        this.idshows = idshows;
        this.moviesIdmovies = moviesIdmovies;
        this.theatresIdtheatres = theatresIdtheatres;
        this.showDate = showDate;
        this.showTime = showTime;
        this.availableSeats = availableSeats;
        this.price = price;
        this.createdAt = createdAt;
    }

    // === Lifecycle Hooks ===
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // === Getters & Setters ===
    public Long getIdshows() { return idshows; }
    public void setIdshows(Long idshows) { this.idshows = idshows; }

    public Long getMoviesIdmovies() { return moviesIdmovies; }
    public void setMoviesIdmovies(Long moviesIdmovies) { this.moviesIdmovies = moviesIdmovies; }

    public Long getTheatresIdtheatres() { return theatresIdtheatres; }
    public void setTheatresIdtheatres(Long theatresIdtheatres) { this.theatresIdtheatres = theatresIdtheatres; }

    public LocalDate getShowDate() { return showDate; }
    public void setShowDate(LocalDate showDate) { this.showDate = showDate; }

    public LocalTime getShowTime() { return showTime; }
    public void setShowTime(LocalTime showTime) { this.showTime = showTime; }

    public Integer getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(Integer availableSeats) { this.availableSeats = availableSeats; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Movie getMovie() { return movie; }
    public Theatre getTheatre() { return theatre; }
}