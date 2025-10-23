package com.nmsCinemas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.nmsCinemas.models.Theatre;
import com.nmsCinemas.service.TheatreService;

import jakarta.validation.Valid;

@Controller
public class TheatreController {

    private final TheatreService theatreService;

    public TheatreController(TheatreService theatreService) {
        this.theatreService = theatreService;
    }

    // List page -> templates/theatreIndex.html
    @GetMapping("/theatres")
    public String list(Model model) {
        model.addAttribute("theatres", theatreService.findAll());
        return "theatreIndex";
    }

    // Create form -> templates/theatreForm.html
    @GetMapping("/theatres/new")
    public String createForm(Model model) {
        model.addAttribute("theatre", new Theatre());
        return "theatreForm";
    }

    // === Create submit ===
    @PostMapping("/theatres")
    public String create(@Valid @ModelAttribute("theatre") Theatre theatre,
                         BindingResult br,
                         RedirectAttributes ra,
                         Model model) {
        if (br.hasErrors()) {
            // Inline message for same-view re-render
            model.addAttribute("errorMessage", "Please correct the highlighted fields.");
            return "theatreForm";
        }
        theatreService.save(theatre);
        ra.addFlashAttribute("successMessage", "Theatre created successfully.");
        return "redirect:/theatres";
    }

    // --- Edit form ---
    @GetMapping("/theatres/{id}/edit")
    public String editForm(@PathVariable("id") Long id,
                           Model model,
                           RedirectAttributes ra) {
        return theatreService.findById(id)
                .map(t -> {
                    model.addAttribute("theatre", t);
                    model.addAttribute("isEdit", true);
                    return "theatreForm";
                })
                .orElseGet(() -> {
                    ra.addFlashAttribute("errorMessage", "Theatre not found.");
                    return "redirect:/theatres";
                });
    }

    // === Update submit ===
    @PostMapping("/theatres/{id}")
    public String update(@PathVariable("id") Long id,
                         @Valid @ModelAttribute("theatre") Theatre theatre,
                         BindingResult br,
                         RedirectAttributes ra,
                         Model model) {
        if (br.hasErrors()) {
            model.addAttribute("isEdit", true);
            model.addAttribute("errorMessage", "Fix the errors and try again.");
            return "theatreForm";
        }
        if (!theatreService.existsById(id)) {
            ra.addFlashAttribute("errorMessage", "Theatre not found.");
            return "redirect:/theatres";
        }
        theatre.setIdtheatres(id);
        theatreService.save(theatre);
        ra.addFlashAttribute("successMessage", "Theatre updated successfully.");
        return "redirect:/theatres";
    }

    // --- Delete ---
    @PostMapping("/theatres/{id}/delete")
    public String delete(@PathVariable("id") Long id, RedirectAttributes ra) {
        if (!theatreService.existsById(id)) {
            ra.addFlashAttribute("errorMessage", "Theatre not found.");
            return "redirect:/theatres";
        }
        theatreService.deleteById(id);
        ra.addFlashAttribute("successMessage", "Theatre deleted.");
        return "redirect:/theatres";
    }
}
