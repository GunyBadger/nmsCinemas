package com.nmsCinemas.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.nmsCinemas.models.User;
import com.nmsCinemas.models.User.Role;
import com.nmsCinemas.service.UserService;

import jakarta.validation.Valid;

@Controller
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // === List all users ===
    @GetMapping("/users")
    public String list(Model model) {
        List<User> users = userService.findAll();
        model.addAttribute("users", users);
        return "userIndex";
    }

    // === Create form ===
    @GetMapping("/users/new")
    public String createForm(Model model) {
        User user = new User();
        user.setRole(Role.USER); // default role
        model.addAttribute("user", user);
        model.addAttribute("isEdit", false);
        return "userForm";
    }

    // === Create submit ===
    @PostMapping("/users")
    public String create(@Valid @ModelAttribute("user") User user,
                         BindingResult br,
                         Model model,
                         RedirectAttributes ra) {

        if (br.hasErrors()) {
            model.addAttribute("isEdit", false);
            model.addAttribute("errorMessage", "Please correct the highlighted fields.");
            return "userForm";
        }

        try {
            userService.save(user);
            ra.addFlashAttribute("successMessage", "User created successfully.");
            return "redirect:/users";
        } catch (Exception ex) {
            model.addAttribute("isEdit", false);
            model.addAttribute("errorMessage", "Error creating user: " + ex.getMessage());
            return "userForm";
        }
    }

    // === Edit form ===
    @GetMapping("/users/{id}/edit")
    public String editForm(@PathVariable("id") Long id,
                          Model model,
                          RedirectAttributes ra) {
        return userService.findById(id)
                .map(user -> {
                    model.addAttribute("user", user);
                    model.addAttribute("isEdit", true);
                    return "userForm";
                })
                .orElseGet(() -> {
                    ra.addFlashAttribute("errorMessage", "User not found.");
                    return "redirect:/users";
                });
    }

    // === Update submit ===
    @PostMapping("/users/{id}")
    public String update(@PathVariable("id") Long id,
                        @Valid @ModelAttribute("user") User user,
                        BindingResult br,
                        Model model,
                        RedirectAttributes ra) {

        if (!userService.existsById(id)) {
            ra.addFlashAttribute("errorMessage", "User not found.");
            return "redirect:/users";
        }

        if (br.hasErrors()) {
            model.addAttribute("isEdit", true);
            model.addAttribute("errorMessage", "Please correct the highlighted fields.");
            return "userForm";
        }

        try {
            user.setIdusers(id);
            userService.save(user);
            ra.addFlashAttribute("successMessage", "User updated successfully.");
            return "redirect:/users";
        } catch (Exception ex) {
            model.addAttribute("isEdit", true);
            model.addAttribute("errorMessage", "Error updating user: " + ex.getMessage());
            return "userForm";
        }
    }

    // === Delete ===
    @PostMapping("/users/{id}/delete")
    public String delete(@PathVariable("id") Long id,
                        RedirectAttributes ra) {
        if (!userService.existsById(id)) {
            ra.addFlashAttribute("errorMessage", "User not found.");
            return "redirect:/users";
        }

        try {
            userService.deleteById(id);
            ra.addFlashAttribute("successMessage", "User deleted successfully.");
        } catch (Exception ex) {
            ra.addFlashAttribute("errorMessage", "Error deleting user: " + ex.getMessage());
        }

        return "redirect:/users";
    }
}