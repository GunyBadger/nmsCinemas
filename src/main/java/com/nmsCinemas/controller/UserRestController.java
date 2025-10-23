package com.nmsCinemas.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.nmsCinemas.models.User;
import com.nmsCinemas.models.Booking;
import com.nmsCinemas.service.UserService;
import com.nmsCinemas.service.BookingService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:4200")
public class UserRestController {

    private final UserService userService;
    private final BookingService bookingService;

    public UserRestController(UserService userService, BookingService bookingService) {
        this.userService = userService;
        this.bookingService = bookingService;
    }

    @GetMapping
    public ResponseEntity<List<User>> list() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> get(@PathVariable("id") Long id) {
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody User user, BindingResult br) {
        if (br.hasErrors()) {
            return ResponseEntity.badRequest()
                    .body("{\"error\": \"" + br.getAllErrors().get(0).getDefaultMessage() + "\"}");
        }

        try {
            User saved = userService.save(user);
            URI location = URI.create("/api/users/" + saved.getIdusers());
            return ResponseEntity.created(location).body(saved);
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body("{\"error\": \"" + ex.getMessage() + "\"}");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable("id") Long id,
                                    @Valid @RequestBody User user,
                                    BindingResult br) {
        if (!userService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        if (br.hasErrors()) {
            return ResponseEntity.badRequest()
                    .body("{\"error\": \"" + br.getAllErrors().get(0).getDefaultMessage() + "\"}");
        }

        try {
            user.setIdusers(id);
            User updated = userService.save(user);
            return ResponseEntity.ok(updated);
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body("{\"error\": \"" + ex.getMessage() + "\"}");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        if (!userService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        try {
            userService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body("{\"error\": \"" + ex.getMessage() + "\"}");
        }
    }

    @GetMapping("/{id}/bookings")
    public ResponseEntity<List<Booking>> getUserBookings(@PathVariable("id") Long id) {
        if (!userService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(bookingService.findByUser(id));
    }
}