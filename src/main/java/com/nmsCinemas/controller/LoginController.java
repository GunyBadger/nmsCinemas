package com.nmsCinemas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.nmsCinemas.models.User;
import com.nmsCinemas.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class LoginController {

    private final UserRepository userRepository;

    public LoginController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Handle login form submission
     */
    @PostMapping("/login")
    public String login(@RequestParam("username") String username,
                       @RequestParam("password") String password,
                       HttpSession session,
                       RedirectAttributes ra) {

        // Find user by username
        User user = userRepository.findByUsername(username).orElse(null);

        // Check if user exists and password matches (plain text for now)
        if (user == null || !user.getPassword().equals(password)) {
            ra.addFlashAttribute("errorMessage", "Invalid username or password.");
            return "redirect:/";
        }

        // Store user in session
        session.setAttribute("loggedInUser", user);
        session.setAttribute("userId", user.getIdusers());
        session.setAttribute("username", user.getUsername());
        session.setAttribute("role", user.getRole().name());

        ra.addFlashAttribute("successMessage", "Welcome back, " + user.getUsername() + "!");

        // Redirect based on role
        if (user.getRole().name().equals("ADMIN")) {
            return "redirect:/admin/dashboard";
        } else {
            return "redirect:/user/dashboard";
        }
    }

    /**
     * User Dashboard - Shows available shows and user's bookings
     */
    @GetMapping("/user/dashboard")
    public String userDashboard(HttpSession session, Model model, RedirectAttributes ra) {
        // Check if user is logged in
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            ra.addFlashAttribute("errorMessage", "Please login first.");
            return "redirect:/";
        }

        model.addAttribute("user", user);
        return "userDashboard";
    }

    /**
     * Admin Dashboard - Full system access
     */
    @GetMapping("/admin/dashboard")
    public String adminDashboard(HttpSession session, Model model, RedirectAttributes ra) {
        // Check if user is logged in and is admin
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            ra.addFlashAttribute("errorMessage", "Please login first.");
            return "redirect:/";
        }

        if (!user.getRole().name().equals("ADMIN")) {
            ra.addFlashAttribute("errorMessage", "Admin access required.");
            return "redirect:/user/dashboard";
        }

        model.addAttribute("user", user);
        return "adminDashboard";
    }

    /**
     * Logout
     */
    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes ra) {
        session.invalidate();
        ra.addFlashAttribute("successMessage", "You have been logged out.");
        return "redirect:/";
    }
}