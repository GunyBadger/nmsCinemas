package com.nmsCinemas.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.nmsCinemas.models.Show;
import com.nmsCinemas.models.ShowId;

public interface ShowRepository extends JpaRepository<Show, ShowId> {

    // Filters used by ShowController/ShowRestController
    List<Show> findByMoviesIdmovies(Long moviesIdmovies);

    List<Show> findByTheatresIdtheatres(Long theatresIdtheatres);

    List<Show> findByShowDate(LocalDate showDate);

    // Manual id generator helper for composite PK (idshows + movie + theatre)
    @Query("select coalesce(max(s.idshows), 0) from Show s")
    Long findMaxShowId();

    // Used by MovieRestController to check if movie has shows before deletion
    long countByMoviesIdmovies(Long moviesIdmovies);

    // ✅ ADD THIS METHOD - Eagerly fetch movie and theatre to avoid lazy loading issues
    @Query("SELECT s FROM Show s LEFT JOIN FETCH s.movie LEFT JOIN FETCH s.theatre")
    List<Show> findAllWithMovieAndTheatre();
}