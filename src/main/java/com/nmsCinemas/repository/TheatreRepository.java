package com.nmsCinemas.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nmsCinemas.models.Theatre;

public interface TheatreRepository extends JpaRepository<Theatre, Long> {
    // Add custom queries later if needed
}
