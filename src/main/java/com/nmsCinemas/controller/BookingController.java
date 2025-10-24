package com.nmsCinemas.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.nmsCinemas.models.Booking;
import com.nmsCinemas.models.BookingId;
import com.nmsCinemas.models.Show;
import com.nmsCinemas.models.ShowId;
import com.nmsCinemas.models.User;
import com.nmsCinemas.service.BookingService;
import com.nmsCinemas.service.ShowService;
import com.nmsCinemas.service.UserService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class BookingController {

    private final BookingService bookingService;
    private final ShowService showService;
    private final UserService userService;

    public BookingController(BookingService bookingService, ShowService showService, UserService userService) {
        this.bookingService = bookingService;
        this.showService = showService;
        this.userService = userService;
    }

    // === List ===
    @GetMapping("/bookings")
    public String list(Model model, HttpSession session, RedirectAttributes ra) {
        System.out.println("═══════════════════════════════════════════");
        System.out.println("📋 LOADING BOOKINGS LIST");
        System.out.println("═══════════════════════════════════════════");

        // Check if user is logged in
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            ra.addFlashAttribute("errorMessage", "Please login first.");
            return "redirect:/";
        }

        try {
            List<Booking> bookings;

            // If admin, show all bookings. If regular user, show only their bookings
            if (loggedInUser.getRole().name().equals("ADMIN")) {
                bookings = bookingService.findAllWithDetails();
                System.out.println("✅ Admin - All bookings loaded: " + bookings.size());
            } else {
                bookings = bookingService.findByUser(loggedInUser.getIdusers());
                System.out.println("✅ User - Own bookings loaded: " + bookings.size());
            }

            model.addAttribute("bookings", bookings);
            return "bookingIndex";
        } catch (Exception e) {
            System.out.println("❌ ERROR loading bookings: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "Error loading bookings: " + e.getMessage());
            model.addAttribute("bookings", new ArrayList<>());
            return "bookingIndex";
        }
    }

    // === Create form ===
    @GetMapping("/bookings/new")
    public String createForm(Model model, HttpSession session, RedirectAttributes ra) {
        // Check if user is logged in
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            ra.addFlashAttribute("errorMessage", "Please login first.");
            return "redirect:/";
        }

        try {
            List<Show> shows = showService.findAllWithDetails();

            System.out.println("═══════════════════════════════════════════");
            System.out.println("📋 BOOKING FORM - LOADING DATA");
            System.out.println("═══════════════════════════════════════════");
            System.out.println("Shows loaded: " + shows.size());
            shows.forEach(s -> {
                System.out.println("  - Show ID: " + s.getIdshows() +
                                 ", Movie: " + (s.getMovie() != null ? s.getMovie().getTitle() : "null") +
                                 ", Theatre: " + (s.getTheatre() != null ? s.getTheatre().getName() : "null") +
                                 ", Price: " + s.getPrice());
            });

            model.addAttribute("booking", new Booking());
            model.addAttribute("shows", shows);

            // Only show user dropdown for admins
            if (loggedInUser.getRole().name().equals("ADMIN")) {
                List<User> users = userService.findAll();
                model.addAttribute("users", users);
                model.addAttribute("isAdmin", true);
                System.out.println("Admin mode - Users loaded: " + users.size());
            } else {
                model.addAttribute("isAdmin", false);
                System.out.println("User mode - Booking for: " + loggedInUser.getUsername());
            }

            model.addAttribute("isEdit", false);
            System.out.println("═══════════════════════════════════════════");
            return "bookingForm";
        } catch (Exception e) {
            System.out.println("❌ ERROR loading booking form:");
            e.printStackTrace();
            ra.addFlashAttribute("errorMessage", "Error loading form: " + e.getMessage());
            return "redirect:/bookings";
        }
    }

    // === Create submit ===
    @PostMapping("/bookings")
    public String create(@ModelAttribute("booking") Booking booking,
                         BindingResult br,
                         HttpSession session,
                         Model model,
                         RedirectAttributes ra) {

        // Check if user is logged in
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            ra.addFlashAttribute("errorMessage", "Please login first.");
            return "redirect:/";
        }

        // ✅ For regular users, automatically set their user ID
        if (!loggedInUser.getRole().name().equals("ADMIN")) {
            booking.setUsersIdusers(loggedInUser.getIdusers());
        }

        // ✅ ADD DEBUG LOGGING
        System.out.println("═══════════════════════════════════════════");
        System.out.println("📝 BOOKING CREATE - RECEIVED DATA:");
        System.out.println("═══════════════════════════════════════════");
        System.out.println("Logged in user: " + loggedInUser.getUsername() + " (Role: " + loggedInUser.getRole() + ")");
        System.out.println("Show ID: " + booking.getShowsIdshows());
        System.out.println("Movie ID: " + booking.getShowsMoviesIdmovies());
        System.out.println("Theatre ID: " + booking.getShowsTheatresIdtheatres());
        System.out.println("User ID: " + booking.getUsersIdusers());
        System.out.println("Number of Seats: " + booking.getNumberOfSeats());
        System.out.println("Seat Numbers: '" + booking.getSeatNumbers() + "'");
        System.out.println("Total Amount: " + booking.getTotalAmount());
        System.out.println("Status: " + booking.getStatus());
        System.out.println("═══════════════════════════════════════════");

        // ✅ MANUAL VALIDATION
        // Validate Show selection
        if (booking.getShowsIdshows() == null ||
            booking.getShowsMoviesIdmovies() == null ||
            booking.getShowsTheatresIdtheatres() == null) {
            br.reject("show.invalid", "Please select a show.");
        }

        // Validate User
        if (booking.getUsersIdusers() == null) {
            br.rejectValue("usersIdusers", "user.invalid", "Please select a user.");
        }

        // Validate Number of Seats
        if (booking.getNumberOfSeats() == null || booking.getNumberOfSeats() < 1) {
            br.rejectValue("numberOfSeats", "seats.invalid", "Number of seats must be at least 1.");
        }

        // Validate Seat Numbers
        if (booking.getSeatNumbers() == null || booking.getSeatNumbers().trim().isEmpty()) {
            br.rejectValue("seatNumbers", "seats.required", "Seat numbers are required.");
        }

        // --- Validate Show exists and seat count matches ---
        if (!br.hasErrors()) {
            ShowId sid = new ShowId(
                booking.getShowsIdshows(),
                booking.getShowsMoviesIdmovies(),
                booking.getShowsTheatresIdtheatres()
            );
            Show show = showService.findById(sid).orElse(null);
            if (show == null) {
                br.reject("show.notFound", "Selected show not found.");
            } else {
                // Seat count validation
                if (booking.getSeatNumbers() != null && booking.getNumberOfSeats() != null) {
                    int count = (int) Arrays.stream(booking.getSeatNumbers().split(","))
                                            .map(String::trim)
                                            .filter(s -> !s.isEmpty())
                                            .count();
                    if (count != booking.getNumberOfSeats()) {
                        br.rejectValue("seatNumbers", "count.mismatch",
                            "Seat list count (" + count + ") must equal Number of seats (" + booking.getNumberOfSeats() + ").");
                    }
                }
                // Auto-calc total if missing
                if (booking.getTotalAmount() == null &&
                    booking.getNumberOfSeats() != null &&
                    show.getPrice() != null) {
                    booking.setTotalAmount(show.getPrice().multiply(BigDecimal.valueOf(booking.getNumberOfSeats())));
                }
            }
        }

        // --- Validate User exists ---
        if (booking.getUsersIdusers() != null && !userService.existsById(booking.getUsersIdusers())) {
            br.rejectValue("usersIdusers", "user.invalid", "Please select a valid user.");
        }

        // --- Validation errors ---
        if (br.hasErrors()) {
            System.out.println("❌ VALIDATION ERRORS: " + br.getAllErrors());
            model.addAttribute("shows", showService.findAllWithDetails());

            if (loggedInUser.getRole().name().equals("ADMIN")) {
                model.addAttribute("users", userService.findAll());
                model.addAttribute("isAdmin", true);
            } else {
                model.addAttribute("isAdmin", false);
            }

            model.addAttribute("errorMessage", "Please correct the highlighted fields.");
            return "bookingForm";
        }

        try {
            Booking saved = bookingService.createBooking(booking);
            System.out.println("✅ BOOKING CREATED: ID = " + saved.getIdbookings());
            ra.addFlashAttribute("successMessage", "Booking created successfully!");
        } catch (IllegalStateException ex) {
            System.out.println("❌ SERVICE ERROR: " + ex.getMessage());
            model.addAttribute("shows", showService.findAllWithDetails());

            if (loggedInUser.getRole().name().equals("ADMIN")) {
                model.addAttribute("users", userService.findAll());
                model.addAttribute("isAdmin", true);
            } else {
                model.addAttribute("isAdmin", false);
            }

            model.addAttribute("errorMessage", ex.getMessage());
            return "bookingForm";
        }

        return "redirect:/bookings";
    }

    // === Edit form ===
    @GetMapping("/bookings/{id}/{sid}/{smid}/{stid}/{uid}/edit")
    public String editForm(@PathVariable("id") Long idbookings,
                           @PathVariable("sid") Long showsIdshows,
                           @PathVariable("smid") Long showsMoviesIdmovies,
                           @PathVariable("stid") Long showsTheatresIdtheatres,
                           @PathVariable("uid") Long usersIdusers,
                           HttpSession session,
                           Model model,
                           RedirectAttributes ra) {

        // Check if user is logged in
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            ra.addFlashAttribute("errorMessage", "Please login first.");
            return "redirect:/";
        }

        BookingId key = new BookingId(idbookings, showsIdshows, showsMoviesIdmovies, showsTheatresIdtheatres, usersIdusers);
        return bookingService.findById(key)
            .map(b -> {
                // Regular users can only edit their own bookings
                if (!loggedInUser.getRole().name().equals("ADMIN") &&
                    !b.getUsersIdusers().equals(loggedInUser.getIdusers())) {
                    ra.addFlashAttribute("errorMessage", "You can only edit your own bookings.");
                    return "redirect:/bookings";
                }

                model.addAttribute("booking", b);
                model.addAttribute("isEdit", true);
                model.addAttribute("shows", showService.findAllWithDetails());
                model.addAttribute("users", userService.findAll());
                return "bookingForm";
            })
            .orElseGet(() -> {
                ra.addFlashAttribute("errorMessage", "Booking not found.");
                return "redirect:/bookings";
            });
    }

    // === Update submit ===
    @PostMapping("/bookings/{id}/{sid}/{smid}/{stid}/{uid}")
    public String update(@PathVariable("id") Long idbookings,
                         @PathVariable("sid") Long showsIdshows,
                         @PathVariable("smid") Long showsMoviesIdmovies,
                         @PathVariable("stid") Long showsTheatresIdtheatres,
                         @PathVariable("uid") Long usersIdusers,
                         @Valid @ModelAttribute("booking") Booking booking,
                         BindingResult br,
                         HttpSession session,
                         Model model,
                         RedirectAttributes ra) {

        // Check if user is logged in
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            ra.addFlashAttribute("errorMessage", "Please login first.");
            return "redirect:/";
        }

        // Regular users can only update their own bookings
        if (!loggedInUser.getRole().name().equals("ADMIN") && !usersIdusers.equals(loggedInUser.getIdusers())) {
            ra.addFlashAttribute("errorMessage", "You can only edit your own bookings.");
            return "redirect:/bookings";
        }

        // Lock PKs to prevent tampering
        booking.setIdbookings(idbookings);
        booking.setShowsIdshows(showsIdshows);
        booking.setShowsMoviesIdmovies(showsMoviesIdmovies);
        booking.setShowsTheatresIdtheatres(showsTheatresIdtheatres);
        booking.setUsersIdusers(usersIdusers);

        // Validate seat count
        if (booking.getSeatNumbers() != null && booking.getNumberOfSeats() != null) {
            int count = (int) Arrays.stream(booking.getSeatNumbers().split(","))
                                    .map(String::trim)
                                    .filter(s -> !s.isEmpty())
                                    .count();
            if (count != booking.getNumberOfSeats()) {
                br.rejectValue("seatNumbers", "count.mismatch",
                    "Seat list count (" + count + ") must equal Number of seats (" + booking.getNumberOfSeats() + ").");
            }
        }

        if (br.hasErrors()) {
            model.addAttribute("isEdit", true);
            model.addAttribute("shows", showService.findAllWithDetails());
            model.addAttribute("users", userService.findAll());
            model.addAttribute("errorMessage", "Fix the errors and try again.");
            return "bookingForm";
        }

        try {
            bookingService.updateBooking(booking);
            ra.addFlashAttribute("successMessage", "Booking updated successfully.");
        } catch (IllegalStateException ex) {
            model.addAttribute("isEdit", true);
            model.addAttribute("shows", showService.findAllWithDetails());
            model.addAttribute("users", userService.findAll());
            model.addAttribute("errorMessage", ex.getMessage());
            return "bookingForm";
        }

        return "redirect:/bookings";
    }

    // === Delete ===
    @PostMapping("/bookings/{id}/{sid}/{smid}/{stid}/{uid}/delete")
    public String delete(@PathVariable("id") Long idbookings,
                         @PathVariable("sid") Long showsIdshows,
                         @PathVariable("smid") Long showsMoviesIdmovies,
                         @PathVariable("stid") Long showsTheatresIdtheatres,
                         @PathVariable("uid") Long usersIdusers,
                         HttpSession session,
                         RedirectAttributes ra) {

        // Check if user is logged in
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            ra.addFlashAttribute("errorMessage", "Please login first.");
            return "redirect:/";
        }

        // Regular users can only delete their own bookings
        if (!loggedInUser.getRole().name().equals("ADMIN") && !usersIdusers.equals(loggedInUser.getIdusers())) {
            ra.addFlashAttribute("errorMessage", "You can only delete your own bookings.");
            return "redirect:/bookings";
        }

        BookingId key = new BookingId(idbookings, showsIdshows, showsMoviesIdmovies, showsTheatresIdtheatres, usersIdusers);

        try {
            bookingService.deleteBooking(key);
            ra.addFlashAttribute("successMessage", "Booking deleted successfully.");
        } catch (IllegalStateException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/bookings";
    }
}