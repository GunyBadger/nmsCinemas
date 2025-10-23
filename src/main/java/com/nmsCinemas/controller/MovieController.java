package com.nmsCinemas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.nmsCinemas.models.Movie;
import com.nmsCinemas.service.MovieService;

import jakarta.validation.Valid;

@Controller
public class MovieController {

    private final MovieService movieService;

    // Constructor injection (no Lombok)
    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    // List all movies -> renders templates/movieIndex.html
    @GetMapping("/movies")
    public String listMovies(Model model) {
        model.addAttribute("movies", movieService.findAll());
        return "movieIndex";
    }

    // === Create form ===
    @GetMapping("/movies/new")
    public String newMovieForm(Model model) {
        model.addAttribute("movie", new Movie());
        return "movieForm";  // templates/movieForm.html
    }

    // === Create submit ===
    @PostMapping("/movies")
    public String createMovie(@Valid @ModelAttribute("movie") Movie movie,
                              BindingResult bindingResult,
                              RedirectAttributes ra,
                              Model model) {
        if (bindingResult.hasErrors()) {
            // Add alert-friendly error message for inline display
            model.addAttribute("errorMessage", "Please correct the highlighted fields.");
            return "movieForm";
        }
        movieService.save(movie);
        ra.addFlashAttribute("successMessage", "Movie created successfully.");
        return "redirect:/movies";
    }

    // --- Edit form ---
    @GetMapping("/movies/{id}/edit")
    public String editMovieForm(@PathVariable("id") Long id, Model model, RedirectAttributes ra) {
        return movieService.findById(id)
                .map(movie -> {
                    model.addAttribute("movie", movie);
                    model.addAttribute("isEdit", true);
                    return "movieForm"; // reuse same template
                })
                .orElseGet(() -> {
                    ra.addFlashAttribute("errorMessage", "Movie not found.");
                    return "redirect:/movies";
                });
    }

    // --- Update submit ---
    @PostMapping("/movies/{id}")
    public String updateMovie(@PathVariable("id") Long id,
                              @Valid @ModelAttribute("movie") Movie movie,
                              BindingResult bindingResult,
                              RedirectAttributes ra,
                              Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", true);
            model.addAttribute("errorMessage", "Fix the errors and try again.");
            return "movieForm";
        }
        if (!movieService.existsById(id)) {
            ra.addFlashAttribute("errorMessage", "Movie not found.");
            return "redirect:/movies";
        }
        movie.setIdmovies(id); // ensure we update the correct row
        movieService.save(movie);
        ra.addFlashAttribute("successMessage", "Movie updated successfully.");
        return "redirect:/movies";
    }

    // --- Delete ---
    @PostMapping("/movies/{id}/delete")
    public String deleteMovie(@PathVariable("id") Long id, RedirectAttributes ra) {
        if (!movieService.existsById(id)) {
            ra.addFlashAttribute("errorMessage", "Movie not found.");
            return "redirect:/movies";
        }
        movieService.deleteById(id);
        ra.addFlashAttribute("successMessage", "Movie deleted.");
        return "redirect:/movies";
    }
}
