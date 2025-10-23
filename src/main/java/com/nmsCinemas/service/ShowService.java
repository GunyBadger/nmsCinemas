package com.nmsCinemas.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nmsCinemas.models.Show;
import com.nmsCinemas.models.ShowId;
import com.nmsCinemas.repository.ShowRepository;

@Service
public class ShowService {

    private final ShowRepository showRepository;

    public ShowService(ShowRepository showRepository) {
        this.showRepository = showRepository;
    }

    /* ---------- Reads ---------- */

    @Transactional(readOnly = true)
    public List<Show> findAll() {
        return showRepository.findAll();
    }


    @Transactional(readOnly = true)
    public List<Show> findAllWithDetails() {
        return showRepository.findAllWithMovieAndTheatre();
    }

    @Transactional(readOnly = true)
    public Optional<Show> findById(ShowId id) {
        return showRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public boolean exists(ShowId id) {
        return showRepository.existsById(id);
    }

    @Transactional(readOnly = true)
    public List<Show> findByMovie(Long movieId) {
        return showRepository.findByMoviesIdmovies(movieId);
    }

    @Transactional(readOnly = true)
    public List<Show> findByTheatre(Long theatreId) {
        return showRepository.findByTheatresIdtheatres(theatreId);
    }

    @Transactional(readOnly = true)
    public List<Show> findByDate(LocalDate date) {
        return showRepository.findByShowDate(date);
    }

    /* ---------- Writes ---------- */

    @Transactional
    public Show save(Show show) {
        if (show.getIdshows() == null) {
            Long max = showRepository.findMaxShowId();
            long next = (max == null ? 1L : max + 1L);
            show.setIdshows(next);
        }
        return showRepository.save(show);
    }

    @Transactional
    public void delete(ShowId id) {
        showRepository.deleteById(id);
    }
}