package com.nmsCinemas.controller;

import java.net.URI;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.nmsCinemas.models.Movie;
import com.nmsCinemas.service.MovieService;
import com.nmsCinemas.repository.ShowRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/movies")
@CrossOrigin(origins = "http://localhost:4200")
public class MovieRestController {

    private final MovieService movieService;
    private final ShowRepository showRepository;

    public MovieRestController(MovieService movieService, ShowRepository showRepository) {
        this.movieService = movieService;
        this.showRepository = showRepository;
    }

    // === READ ALL ===
    @GetMapping
    public ResponseEntity<List<Movie>> getAllMovies() {
        List<Movie> movies = movieService.findAll();
        return ResponseEntity.ok(movies);
    }

    // === READ ONE ===
    @GetMapping("/{id}")
    public ResponseEntity<Movie> getMovie(@PathVariable Long id) {
        return movieService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // === CREATE === (201 + Location)
    @PostMapping
    public ResponseEntity<?> createMovie(@Valid @RequestBody Movie movie, BindingResult br) {
        // Check for validation errors
        if (br.hasErrors()) {
            return ResponseEntity.badRequest()
                    .body("Validation error: " + br.getAllErrors().get(0).getDefaultMessage());
        }

        try {
            Movie saved = movieService.save(movie);
            URI location = URI.create("/api/movies/" + saved.getIdmovies());
            return ResponseEntity.created(location).body(saved);
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body("Error creating movie: " + ex.getMessage());
        }
    }

    // === UPDATE === ✅ FIXED
    @PutMapping("/{id}")
    public ResponseEntity<?> updateMovie(@PathVariable Long id,
                                        @Valid @RequestBody Movie movie,
                                        BindingResult br) {
        // Check if movie exists
        if (!movieService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        // Check for validation errors
        if (br.hasErrors()) {
            return ResponseEntity.badRequest()
                    .body("Validation error: " + br.getAllErrors().get(0).getDefaultMessage());
        }

        try {
            // ✅ CRITICAL: Force the ID to match the path parameter
            movie.setIdmovies(id);

            // Save the updated movie
            Movie updated = movieService.save(movie);

            return ResponseEntity.ok(updated);
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body("Error updating movie: " + ex.getMessage());
        }
    }

    // === DELETE === ✅ FIXED (409 when referenced by shows)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMovie(@PathVariable Long id) {
        // Check if movie exists
        if (!movieService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        // Check if movie is referenced by any shows
        long refCount = showRepository.countByMoviesIdmovies(id);
        if (refCount > 0) {
            return ResponseEntity.status(409)
                    .header("X-Reason", "Movie has " + refCount + " scheduled show(s)")
                    .body("{\"error\": \"Cannot delete movie. It has " + refCount + " scheduled show(s).\"}");
        }

        try {
            movieService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (DataIntegrityViolationException ex) {
            // Defensive catch in case other FKs appear later
            return ResponseEntity.status(409)
                    .header("X-Reason", "Movie is referenced by existing records")
                    .body("{\"error\": \"Cannot delete movie. It is referenced by existing records.\"}");
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body("{\"error\": \"Error deleting movie: " + ex.getMessage() + "\"}");
        }
    }
}