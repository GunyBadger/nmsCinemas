package com.nmsCinemas.models;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "theatres")
public class Theatre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idtheatres")
    private Long idtheatres;

    @NotBlank(message = "Name is required.")
    @Size(max = 45, message = "Name must be at most 45 characters.")
    @Column(nullable = false, length = 45)
    private String name;

    @NotBlank(message = "Location is required.")
    @Size(max = 100, message = "Location must be at most 100 characters.")
    @Column(nullable = false, length = 100)
    private String location;

    @NotNull(message = "Total seats is required.")
    @Min(value = 1, message = "Total seats must be at least 1.")
    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;

    // ✅ CRITICAL FIX: Add updatable = false
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Theatre() {}

    public Theatre(Long idtheatres, String name, String location, Integer totalSeats, LocalDateTime createdAt) {
        this.idtheatres = idtheatres;
        this.name = name;
        this.location = location;
        this.totalSeats = totalSeats;
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getIdtheatres() { return idtheatres; }
    public void setIdtheatres(Long idtheatres) { this.idtheatres = idtheatres; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Integer getTotalSeats() { return totalSeats; }
    public void setTotalSeats(Integer totalSeats) { this.totalSeats = totalSeats; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}