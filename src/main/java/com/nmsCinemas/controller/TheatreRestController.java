package com.nmsCinemas.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nmsCinemas.models.Theatre;
import com.nmsCinemas.service.TheatreService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/theatres")
@CrossOrigin(origins = "http://localhost:4200") // Allow Angular frontend during development
public class TheatreRestController {

    private final TheatreService theatreService;

    public TheatreRestController(TheatreService theatreService) {
        this.theatreService = theatreService;
    }

    // === READ ALL ===
    @GetMapping
    public List<Theatre> getAllTheatres() {
        return theatreService.findAll();
    }

    // === READ ONE ===
    @GetMapping("/{id}")
    public ResponseEntity<Theatre> getTheatre(@PathVariable Long id) {
        return theatreService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // === CREATE (201 Created + Location header) ===
    @PostMapping
    public ResponseEntity<Theatre> createTheatre(@Valid @RequestBody Theatre theatre) {
        // Prevent client-supplied ID on create
        theatre.setIdtheatres(null);

        Theatre saved = theatreService.save(theatre);
        URI location = URI.create("/api/theatres/" + saved.getIdtheatres());
        return ResponseEntity.created(location).body(saved);
    }

    // === UPDATE ===
    @PutMapping("/{id}")
    public ResponseEntity<Theatre> updateTheatre(@PathVariable Long id, @Valid @RequestBody Theatre theatre) {
        if (!theatreService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        theatre.setIdtheatres(id);
        Theatre updated = theatreService.save(theatre);
        return ResponseEntity.ok(updated);
    }

    // === DELETE ===
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTheatre(@PathVariable Long id) {
        if (!theatreService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        theatreService.deleteById(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}
