package com.nmsCinemas.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nmsCinemas.models.Movie;
import com.nmsCinemas.repository.MovieRepository;

@Service
@Transactional(readOnly = true)
public class MovieService {

    private final MovieRepository movieRepository;

    // Constructor injection (no Lombok)
    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public List<Movie> findAll() {
        return movieRepository.findAll();
    }

    public Optional<Movie> findById(Long id) {
        return movieRepository.findById(id);
    }

    @Transactional
    public Movie save(Movie movie) {
        return movieRepository.save(movie);
    }

    @Transactional
    public void deleteById(Long id) {
        movieRepository.deleteById(id);
    }

    public boolean existsById(Long id) {
        return movieRepository.existsById(id);
    }
}
