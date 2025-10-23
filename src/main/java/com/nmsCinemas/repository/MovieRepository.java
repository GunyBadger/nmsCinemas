package com.nmsCinemas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.nmsCinemas.models.Movie;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    // Add custom query methods later as needed
}
