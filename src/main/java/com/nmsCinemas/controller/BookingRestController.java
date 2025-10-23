package com.nmsCinemas.controller;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.nmsCinemas.models.Booking;
import com.nmsCinemas.models.BookingId;
import com.nmsCinemas.models.Show;
import com.nmsCinemas.models.ShowId;
import com.nmsCinemas.service.BookingService;
import com.nmsCinemas.service.ShowService;
import com.nmsCinemas.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "http://localhost:4200")
public class BookingRestController {

    private final BookingService bookingService;
    private final ShowService showService;
    private final UserService userService;

    public BookingRestController(BookingService bookingService,
                                 ShowService showService,
                                 UserService userService) {
        this.bookingService = bookingService;
        this.showService = showService;
        this.userService = userService;
    }

    // ========= READ (list) =========
    // /api/bookings
    // /api/bookings?userId=1
    // /api/bookings?sid=10&smid=1&stid=5
    // /api/bookings?details=true (eager show+user)
    @GetMapping
    public ResponseEntity<List<Booking>> list(
            @RequestParam(name = "userId", required = false) Long usersIdusers,
            @RequestParam(name = "sid", required = false) Long showsIdshows,
            @RequestParam(name = "smid", required = false) Long showsMoviesIdmovies,
            @RequestParam(name = "stid", required = false) Long showsTheatresIdtheatres,
            @RequestParam(name = "details", defaultValue = "true") boolean details) {

        try {
            List<Booking> bookings;

            if (usersIdusers != null) {
                bookings = bookingService.findByUser(usersIdusers);
            } else if (showsIdshows != null && showsMoviesIdmovies != null && showsTheatresIdtheatres != null) {
                bookings = bookingService.findByShowWithDetails(showsIdshows, showsMoviesIdmovies, showsTheatresIdtheatres);
            } else {
                bookings = details ? bookingService.findAllWithDetails() : bookingService.findAll();
            }

            return ResponseEntity.ok(bookings);
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ========= READ (one) =========
    @GetMapping("/{id}/{sid}/{smid}/{stid}/{uid}")
    public ResponseEntity<Booking> get(
            @PathVariable("id") Long idbookings,
            @PathVariable("sid") Long showsIdshows,
            @PathVariable("smid") Long showsMoviesIdmovies,
            @PathVariable("stid") Long showsTheatresIdtheatres,
            @PathVariable("uid") Long usersIdusers) {

        BookingId key = new BookingId(idbookings, showsIdshows, showsMoviesIdmovies, showsTheatresIdtheatres, usersIdusers);
        return bookingService.findById(key)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ========= CREATE (201 + Location) =========
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody Booking booking, BindingResult br) {
        // ✅ Check validation errors first
        if (br.hasErrors()) {
            return ResponseEntity.badRequest()
                    .body("{\"error\": \"" + br.getAllErrors().get(0).getDefaultMessage() + "\"}");
        }

        // Validate Show selection
        if (booking.getShowsIdshows() == null ||
            booking.getShowsMoviesIdmovies() == null ||
            booking.getShowsTheatresIdtheatres() == null) {
            return ResponseEntity.badRequest()
                    .body("{\"error\": \"Please select a valid show.\"}");
        }

        ShowId sid = new ShowId(
                booking.getShowsIdshows(),
                booking.getShowsMoviesIdmovies(),
                booking.getShowsTheatresIdtheatres()
        );
        Show show = showService.findById(sid).orElse(null);
        if (show == null) {
            return ResponseEntity.badRequest()
                    .body("{\"error\": \"Selected show not found.\"}");
        }

        // Validate User
        if (booking.getUsersIdusers() == null || !userService.existsById(booking.getUsersIdusers())) {
            return ResponseEntity.badRequest()
                    .body("{\"error\": \"Please select a valid user.\"}");
        }

        // Validate seat list count matches numberOfSeats
        if (booking.getSeatNumbers() != null && booking.getNumberOfSeats() != null) {
            int count = (int) Arrays.stream(booking.getSeatNumbers().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .count();
            if (count != booking.getNumberOfSeats()) {
                return ResponseEntity.badRequest()
                        .body("{\"error\": \"Seat list count (" + count + ") must equal Number of seats (" +
                              booking.getNumberOfSeats() + ").\"}");
            }
        }

        // Auto-calculate total if absent
        if (booking.getTotalAmount() == null &&
            booking.getNumberOfSeats() != null &&
            show.getPrice() != null) {
            booking.setTotalAmount(show.getPrice().multiply(BigDecimal.valueOf(booking.getNumberOfSeats())));
        }

        try {
            Booking saved = bookingService.createBooking(booking); // handles seat deduction if CONFIRMED
            URI location = URI.create("/api/bookings/" +
                    saved.getIdbookings() + "/" +
                    saved.getShowsIdshows() + "/" +
                    saved.getShowsMoviesIdmovies() + "/" +
                    saved.getShowsTheatresIdtheatres() + "/" +
                    saved.getUsersIdusers());
            return ResponseEntity.created(location).body(saved);
        } catch (IllegalStateException ex) {
            return ResponseEntity.badRequest()
                    .body("{\"error\": \"" + ex.getMessage() + "\"}");
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body("{\"error\": \"Error creating booking: " + ex.getMessage() + "\"}");
        }
    }

    // ========= UPDATE (PK locked to path) =========
    @PutMapping("/{id}/{sid}/{smid}/{stid}/{uid}")
    public ResponseEntity<?> update(
            @PathVariable("id") Long idbookings,
            @PathVariable("sid") Long showsIdshows,
            @PathVariable("smid") Long showsMoviesIdmovies,
            @PathVariable("stid") Long showsTheatresIdtheatres,
            @PathVariable("uid") Long usersIdusers,
            @Valid @RequestBody Booking booking,
            BindingResult br) {

        // Check if booking exists
        BookingId key = new BookingId(idbookings, showsIdshows, showsMoviesIdmovies, showsTheatresIdtheatres, usersIdusers);
        if (!bookingService.existsById(key)) {
            return ResponseEntity.notFound().build();
        }

        // ✅ Check validation errors
        if (br.hasErrors()) {
            return ResponseEntity.badRequest()
                    .body("{\"error\": \"" + br.getAllErrors().get(0).getDefaultMessage() + "\"}");
        }

        // ✅ CRITICAL: Lock PKs to path values (prevent tampering)
        booking.setIdbookings(idbookings);
        booking.setShowsIdshows(showsIdshows);
        booking.setShowsMoviesIdmovies(showsMoviesIdmovies);
        booking.setShowsTheatresIdtheatres(showsTheatresIdtheatres);
        booking.setUsersIdusers(usersIdusers);

        // Validate seat list count matches numberOfSeats
        if (booking.getSeatNumbers() != null && booking.getNumberOfSeats() != null) {
            int count = (int) Arrays.stream(booking.getSeatNumbers().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .count();
            if (count != booking.getNumberOfSeats()) {
                return ResponseEntity.badRequest()
                        .body("{\"error\": \"Seat list count (" + count + ") must equal Number of seats (" +
                              booking.getNumberOfSeats() + ").\"}");
            }
        }

        try {
            Booking updated = bookingService.updateBooking(booking); // handles seat adjustment
            return ResponseEntity.ok(updated);
        } catch (IllegalStateException ex) {
            return ResponseEntity.badRequest()
                    .body("{\"error\": \"" + ex.getMessage() + "\"}");
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body("{\"error\": \"Error updating booking: " + ex.getMessage() + "\"}");
        }
    }

    // ========= DELETE (restore seats if needed) =========
    @DeleteMapping("/{id}/{sid}/{smid}/{stid}/{uid}")
    public ResponseEntity<?> delete(
            @PathVariable("id") Long idbookings,
            @PathVariable("sid") Long showsIdshows,
            @PathVariable("smid") Long showsMoviesIdmovies,
            @PathVariable("stid") Long showsTheatresIdtheatres,
            @PathVariable("uid") Long usersIdusers) {

        BookingId key = new BookingId(idbookings, showsIdshows, showsMoviesIdmovies, showsTheatresIdtheatres, usersIdusers);

        // Check if booking exists
        if (!bookingService.existsById(key)) {
            return ResponseEntity.notFound().build();
        }

        try {
            bookingService.deleteBooking(key); // restores seats if booking was CONFIRMED
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException ex) {
            return ResponseEntity.badRequest()
                    .body("{\"error\": \"" + ex.getMessage() + "\"}");
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body("{\"error\": \"Error deleting booking: " + ex.getMessage() + "\"}");
        }
    }
}