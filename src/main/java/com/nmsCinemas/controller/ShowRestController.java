package com.nmsCinemas.controller;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.nmsCinemas.models.Show;
import com.nmsCinemas.models.ShowId;
import com.nmsCinemas.models.Theatre;
import com.nmsCinemas.service.MovieService;
import com.nmsCinemas.service.ShowService;
import com.nmsCinemas.service.TheatreService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/shows")
@CrossOrigin(origins = "http://localhost:4200")
public class ShowRestController {

    private final ShowService showService;
    private final MovieService movieService;
    private final TheatreService theatreService;

    public ShowRestController(ShowService showService,
                              MovieService movieService,
                              TheatreService theatreService) {
        this.showService = showService;
        this.movieService = movieService;
        this.theatreService = theatreService;
    }

    // === READ (all, with optional filters and eager details) ===
    // /api/shows
    // /api/shows?movieId=1
    // /api/shows?theatreId=2
    // /api/shows?date=2025-01-01
    // /api/shows?details=true -> eager fetch movie & theatre
    @GetMapping
    public ResponseEntity<List<Show>> list(
            @RequestParam(name = "movieId", required = false) Long movieId,
            @RequestParam(name = "theatreId", required = false) Long theatreId,
            @RequestParam(name = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(name = "details", required = false, defaultValue = "false") boolean details) {

        try {
            List<Show> shows;

            if (movieId != null) {
                shows = showService.findByMovie(movieId);
            } else if (theatreId != null) {
                shows = showService.findByTheatre(theatreId);
            } else if (date != null) {
                shows = showService.findByDate(date);
            } else {
                shows = details ? showService.findAllWithDetails() : showService.findAll();
            }

            return ResponseEntity.ok(shows);
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // === READ ONE ===
    @GetMapping("/{idshows}/{movieId}/{theatreId}")
    public ResponseEntity<Show> get(
            @PathVariable Long idshows,
            @PathVariable Long movieId,
            @PathVariable Long theatreId) {

        ShowId key = new ShowId(idshows, movieId, theatreId);
        return showService.findById(key)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // === CREATE (201 Created + Location header) ===
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody Show show, BindingResult br) {
        // ✅ Check validation errors first
        if (br.hasErrors()) {
            return ResponseEntity.badRequest()
                    .body("{\"error\": \"" + br.getAllErrors().get(0).getDefaultMessage() + "\"}");
        }

        // Ensure DB generates idshows
        show.setIdshows(null);

        // Validate Movie FK
        if (show.getMoviesIdmovies() == null || !movieService.existsById(show.getMoviesIdmovies())) {
            return ResponseEntity.badRequest()
                    .body("{\"error\": \"Invalid movie: please select an existing movie.\"}");
        }

        // Validate Theatre FK
        if (show.getTheatresIdtheatres() == null || !theatreService.existsById(show.getTheatresIdtheatres())) {
            return ResponseEntity.badRequest()
                    .body("{\"error\": \"Invalid theatre: please select an existing theatre.\"}");
        }

        // Capacity check
        Theatre theatre = theatreService.findById(show.getTheatresIdtheatres()).orElse(null);
        if (theatre != null && show.getAvailableSeats() != null
                && show.getAvailableSeats() > theatre.getTotalSeats()) {
            return ResponseEntity.badRequest()
                    .body("{\"error\": \"Available seats (" + show.getAvailableSeats() +
                          ") cannot exceed theatre capacity (" + theatre.getTotalSeats() + ").\"}");
        }

        try {
            Show saved = showService.save(show);

            // Composite key in Location header
            URI location = URI.create("/api/shows/" + saved.getIdshows() + "/"
                                      + saved.getMoviesIdmovies() + "/"
                                      + saved.getTheatresIdtheatres());
            return ResponseEntity.created(location).body(saved);
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body("{\"error\": \"Error creating show: " + ex.getMessage() + "\"}");
        }
    }

    // === UPDATE (PK locked in path) ===
    @PutMapping("/{idshows}/{movieId}/{theatreId}")
    public ResponseEntity<?> update(
            @PathVariable Long idshows,
            @PathVariable Long movieId,
            @PathVariable Long theatreId,
            @Valid @RequestBody Show show,
            BindingResult br) {

        // Check if show exists
        ShowId key = new ShowId(idshows, movieId, theatreId);
        if (!showService.exists(key)) {
            return ResponseEntity.notFound().build();
        }

        // ✅ Check validation errors
        if (br.hasErrors()) {
            return ResponseEntity.badRequest()
                    .body("{\"error\": \"" + br.getAllErrors().get(0).getDefaultMessage() + "\"}");
        }

        // ✅ CRITICAL: Lock the PK to path values (prevent tampering)
        show.setIdshows(idshows);
        show.setMoviesIdmovies(movieId);
        show.setTheatresIdtheatres(theatreId);

        // Capacity check
        Theatre theatre = theatreService.findById(theatreId).orElse(null);
        if (theatre != null && show.getAvailableSeats() != null
                && show.getAvailableSeats() > theatre.getTotalSeats()) {
            return ResponseEntity.badRequest()
                    .body("{\"error\": \"Available seats (" + show.getAvailableSeats() +
                          ") cannot exceed theatre capacity (" + theatre.getTotalSeats() + ").\"}");
        }

        try {
            Show updated = showService.save(show);
            return ResponseEntity.ok(updated);
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body("{\"error\": \"Error updating show: " + ex.getMessage() + "\"}");
        }
    }

    // === DELETE ===
    @DeleteMapping("/{idshows}/{movieId}/{theatreId}")
    public ResponseEntity<?> delete(
            @PathVariable Long idshows,
            @PathVariable Long movieId,
            @PathVariable Long theatreId) {

        ShowId key = new ShowId(idshows, movieId, theatreId);

        // Check if show exists
        if (!showService.exists(key)) {
            return ResponseEntity.notFound().build();
        }

        try {
            showService.delete(key);
            return ResponseEntity.noContent().build();
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body("{\"error\": \"Error deleting show: " + ex.getMessage() + "\"}");
        }
    }
}