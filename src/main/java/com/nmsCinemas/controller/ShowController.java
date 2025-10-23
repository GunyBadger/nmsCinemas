package com.nmsCinemas.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.nmsCinemas.models.Show;
import com.nmsCinemas.models.ShowId;
import com.nmsCinemas.models.Theatre;
import com.nmsCinemas.service.MovieService;
import com.nmsCinemas.service.ShowService;
import com.nmsCinemas.service.TheatreService;

import jakarta.validation.Valid;

@Controller
public class ShowController {

    private final ShowService showService;
    private final MovieService movieService;
    private final TheatreService theatreService;

    public ShowController(ShowService showService,
                          MovieService movieService,
                          TheatreService theatreService) {
        this.showService = showService;
        this.movieService = movieService;
        this.theatreService = theatreService;
    }

    /**
     * List shows with optional filters:
     *  - /shows
     *  - /shows?movieId=1
     *  - /shows?theatreId=2
     *  - /shows?date=2025-01-01
     */
    @GetMapping("/shows")
    public String listShows(@RequestParam(name = "movieId", required = false) Long movieId,
                            @RequestParam(name = "theatreId", required = false) Long theatreId,
                            @RequestParam(name = "date", required = false)
                            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                            Model model) {

        List<Show> shows;
        if (movieId != null) {
            shows = showService.findByMovie(movieId);
        } else if (theatreId != null) {
            shows = showService.findByTheatre(theatreId);
        } else if (date != null) {
            shows = showService.findByDate(date);
        } else {
            // Eager load movie & theatre for the list page to avoid N+1
            shows = showService.findAllWithDetails();
        }

        model.addAttribute("shows", shows);
        return "showIndex";
    }

    /** Show the create form */
    @GetMapping("/shows/new")
    public String newShowForm(Model model) {
        model.addAttribute("show", new Show());
        model.addAttribute("movies", movieService.findAll());     // for dropdown
        model.addAttribute("theatres", theatreService.findAll()); // for dropdown
        return "showForm";
    }

    /** Create submit */
    @PostMapping("/shows")
    public String createShow(@Valid @ModelAttribute("show") Show show,
                             BindingResult br,
                             Model model,
                             RedirectAttributes ra) {
        // Cross-field validation:
        // 1) ensure selected Movie & Theatre exist
        if (show.getMoviesIdmovies() == null || !movieService.existsById(show.getMoviesIdmovies())) {
            br.rejectValue("moviesIdmovies", "invalid.movie", "Please select a valid movie.");
        }
        if (show.getTheatresIdtheatres() == null || !theatreService.existsById(show.getTheatresIdtheatres())) {
            br.rejectValue("theatresIdtheatres", "invalid.theatre", "Please select a valid theatre.");
        }

        // 2) availableSeats <= theatre.totalSeats (when theatre is valid)
        if (show.getTheatresIdtheatres() != null) {
            Theatre theatre = theatreService.findById(show.getTheatresIdtheatres()).orElse(null);
            if (theatre != null && show.getAvailableSeats() != null
                    && show.getAvailableSeats() > theatre.getTotalSeats()) {
                br.rejectValue("availableSeats", "tooMany",
                        "Available seats cannot exceed theatre capacity (" + theatre.getTotalSeats() + ").");
            }
        }

        if (br.hasErrors()) {
            model.addAttribute("movies", movieService.findAll());
            model.addAttribute("theatres", theatreService.findAll());
            model.addAttribute("errorMessage", "Please correct the highlighted fields.");
            return "showForm";
        }

        showService.save(show);
        ra.addFlashAttribute("successMessage", "Show created successfully.");
        return "redirect:/shows";
    }

    // --- Edit form ---
    @GetMapping("/shows/{idshows}/{movieId}/{theatreId}/edit")
    public String editShowForm(@PathVariable("idshows") Long idshows,
                               @PathVariable("movieId") Long movieId,
                               @PathVariable("theatreId") Long theatreId,
                               Model model,
                               RedirectAttributes ra) {

        ShowId key = new ShowId(idshows, movieId, theatreId);
        return showService.findById(key)
                .map(s -> {
                    model.addAttribute("show", s);
                    // Lock movie/theatre on edit to avoid changing composite PK
                    model.addAttribute("isEdit", true);
                    // Provide dropdown data (useful if later you allow changes)
                    model.addAttribute("movies", movieService.findAll());
                    model.addAttribute("theatres", theatreService.findAll());
                    return "showForm";
                })
                .orElseGet(() -> {
                    ra.addFlashAttribute("errorMessage", "Show not found.");
                    return "redirect:/shows";
                });
    }

    // --- Update submit ---
    // Note: Movie/Theatre remain locked during edit to avoid PK tampering.
    @PostMapping("/shows/{idshows}/{movieId}/{theatreId}")
    public String updateShow(@PathVariable("idshows") Long idshows,
                             @PathVariable("movieId") Long movieId,
                             @PathVariable("theatreId") Long theatreId,
                             @Valid @ModelAttribute("show") Show show,
                             BindingResult br,
                             Model model,
                             RedirectAttributes ra) {

        ShowId key = new ShowId(idshows, movieId, theatreId);
        if (!showService.exists(key)) {
            ra.addFlashAttribute("errorMessage", "Show not found.");
            return "redirect:/shows";
        }

        // Force the key to the path values (prevents PK changes from payload)
        show.setIdshows(idshows);
        show.setMoviesIdmovies(movieId);
        show.setTheatresIdtheatres(theatreId);

        // Capacity check
        Theatre theatre = theatreService.findById(theatreId).orElse(null);
        if (theatre != null && show.getAvailableSeats() != null
                && show.getAvailableSeats() > theatre.getTotalSeats()) {
            br.rejectValue("availableSeats", "tooMany",
                    "Available seats cannot exceed theatre capacity (" + theatre.getTotalSeats() + ").");
        }

        if (br.hasErrors()) {
            model.addAttribute("isEdit", true);
            model.addAttribute("movies", movieService.findAll());
            model.addAttribute("theatres", theatreService.findAll());
            model.addAttribute("errorMessage", "Fix the errors and try again.");
            return "showForm";
        }

        showService.save(show); // same save path works for update
        ra.addFlashAttribute("successMessage", "Show updated successfully.");
        return "redirect:/shows";
    }

    // --- Delete ---
    @PostMapping("/shows/{idshows}/{movieId}/{theatreId}/delete")
    public String deleteShow(@PathVariable("idshows") Long idshows,
                             @PathVariable("movieId") Long movieId,
                             @PathVariable("theatreId") Long theatreId,
                             RedirectAttributes ra) {
        ShowId key = new ShowId(idshows, movieId, theatreId);
        if (!showService.exists(key)) {
            ra.addFlashAttribute("errorMessage", "Show not found.");
            return "redirect:/shows";
        }
        showService.delete(key);
        ra.addFlashAttribute("successMessage", "Show deleted.");
        return "redirect:/shows";
    }
}
