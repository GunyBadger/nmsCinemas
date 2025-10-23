package com.nmsCinemas.service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nmsCinemas.models.Booking;
import com.nmsCinemas.models.BookingId;
import com.nmsCinemas.models.Show;
import com.nmsCinemas.models.ShowId;
import com.nmsCinemas.repository.BookingRepository;
import com.nmsCinemas.repository.ShowRepository;
import com.nmsCinemas.repository.UserRepository;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ShowRepository showRepository;
    private final UserRepository userRepository;

    public BookingService(BookingRepository bookingRepository,
                          ShowRepository showRepository,
                          UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.showRepository = showRepository;
        this.userRepository = userRepository;
    }

    /* ---------- Reads ---------- */

    @Transactional(readOnly = true)
    public List<Booking> findAll() {
        return bookingRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Booking> findAllWithDetails() {
        // ✅ NOW uses eager fetch from repository
        return bookingRepository.findAllWithDetails();
    }

    @Transactional(readOnly = true)
    public Optional<Booking> findById(BookingId id) {
        return bookingRepository.findById(id);
    }

    // ✅ ADD THIS METHOD - Used by controllers
    @Transactional(readOnly = true)
    public boolean existsById(BookingId id) {
        return bookingRepository.existsById(id);
    }

    @Transactional(readOnly = true)
    public List<Booking> findByUser(Long userId) {
        return bookingRepository.findByUsersIdusers(userId);
    }

    // ✅ ADD THIS METHOD - Used by BookingRestController
    @Transactional(readOnly = true)
    public List<Booking> findByShowWithDetails(Long showsIdshows, Long showsMoviesIdmovies, Long showsTheatresIdtheatres) {
        return bookingRepository.findByShowsIdshowsAndShowsMoviesIdmoviesAndShowsTheatresIdtheatres(
            showsIdshows, showsMoviesIdmovies, showsTheatresIdtheatres
        );
    }

    /* ---------- Create / Update helpers ---------- */

    private static int countSeats(String seatNumbers) {
        if (seatNumbers == null || seatNumbers.isBlank()) return 0;
        return (int) Arrays.stream(seatNumbers.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .count();
    }

    private static BigDecimal multiply(BigDecimal a, int b) {
        return (a == null ? BigDecimal.ZERO : a).multiply(BigDecimal.valueOf(b));
    }

    /* ---------- Writes ---------- */

    /**
     * Create a booking.
     * - Assigns next idbookings if null (manual sequence).
     * - Validates show/user exist.
     * - Validates seats count and availability.
     * - Deducts seats when status == CONFIRMED.
     * - Auto-calculates totalAmount if null (price * seats).
     */
    @Transactional
    public Booking createBooking(Booking booking) {
        // Manual sequence for composite PK part
        if (booking.getIdbookings() == null) {
            Long max = bookingRepository.findMaxBookingId();
            long next = (max == null ? 1L : max + 1L);
            booking.setIdbookings(next);
        }

        // Validate Show exists
        ShowId sid = new ShowId(
                booking.getShowsIdshows(),
                booking.getShowsMoviesIdmovies(),
                booking.getShowsTheatresIdtheatres());
        Show show = showRepository.findById(sid).orElseThrow(
                () -> new IllegalStateException("Selected show not found.")
        );

        // Validate User exists
        if (booking.getUsersIdusers() == null || !userRepository.existsById(booking.getUsersIdusers())) {
            throw new IllegalStateException("Please select a valid user.");
        }

        // Seats count must match
        int count = countSeats(booking.getSeatNumbers());
        if (booking.getNumberOfSeats() == null || booking.getNumberOfSeats() <= 0) {
            throw new IllegalStateException("Number of seats must be ≥ 1.");
        }
        if (count != booking.getNumberOfSeats()) {
            throw new IllegalStateException("Seat list count (" + count + ") must equal Number of seats (" + booking.getNumberOfSeats() + ").");
        }

        // Check availability if confirming
        boolean confirm = booking.getStatus() != null && "CONFIRMED".equalsIgnoreCase(booking.getStatus().name());
        if (confirm) {
            if (show.getAvailableSeats() == null || show.getAvailableSeats() < booking.getNumberOfSeats()) {
                throw new IllegalStateException("Not enough seats available.");
            }
            show.setAvailableSeats(show.getAvailableSeats() - booking.getNumberOfSeats());
            showRepository.save(show);
        }

        // Auto total if absent
        if (booking.getTotalAmount() == null) {
            booking.setTotalAmount(multiply(show.getPrice(), booking.getNumberOfSeats()));
        }

        return bookingRepository.save(booking);
    }

    /**
     * Update a booking (same composite PK).
     * We recompute seat availability deltas if status or numberOfSeats changed.
     */
    @Transactional
    public Booking updateBooking(Booking booking) {
        BookingId id = new BookingId(
                booking.getIdbookings(),
                booking.getShowsIdshows(),
                booking.getShowsMoviesIdmovies(),
                booking.getShowsTheatresIdtheatres(),
                booking.getUsersIdusers());

        Booking current = bookingRepository.findById(id).orElseThrow(
                () -> new IllegalStateException("Booking not found.")
        );

        ShowId sid = new ShowId(
                booking.getShowsIdshows(),
                booking.getShowsMoviesIdmovies(),
                booking.getShowsTheatresIdtheatres());
        Show show = showRepository.findById(sid).orElseThrow(
                () -> new IllegalStateException("Show not found.")
        );

        // Validate seat list
        int count = countSeats(booking.getSeatNumbers());
        if (booking.getNumberOfSeats() == null || booking.getNumberOfSeats() <= 0) {
            throw new IllegalStateException("Number of seats must be ≥ 1.");
        }
        if (count != booking.getNumberOfSeats()) {
            throw new IllegalStateException("Seat list count (" + count + ") must equal Number of seats (" + booking.getNumberOfSeats() + ").");
        }

        // Handle seat availability delta
        boolean wasConfirmed = current.getStatus() != null && "CONFIRMED".equalsIgnoreCase(current.getStatus().name());
        boolean nowConfirmed = booking.getStatus() != null && "CONFIRMED".equalsIgnoreCase(booking.getStatus().name());

        int oldSeats = current.getNumberOfSeats() == null ? 0 : current.getNumberOfSeats();
        int newSeats = booking.getNumberOfSeats();

        if (wasConfirmed && !nowConfirmed) {
            // restore previously taken seats
            show.setAvailableSeats(show.getAvailableSeats() + oldSeats);
        } else if (!wasConfirmed && nowConfirmed) {
            // deduct fresh
            if (show.getAvailableSeats() < newSeats) {
                throw new IllegalStateException("Not enough seats available to confirm.");
            }
            show.setAvailableSeats(show.getAvailableSeats() - newSeats);
        } else if (wasConfirmed && nowConfirmed) {
            // adjust delta
            int delta = newSeats - oldSeats; // positive means need more seats
            if (delta > 0) {
                if (show.getAvailableSeats() < delta) {
                    throw new IllegalStateException("Not enough seats available for the change.");
                }
                show.setAvailableSeats(show.getAvailableSeats() - delta);
            } else if (delta < 0) {
                show.setAvailableSeats(show.getAvailableSeats() - delta); // delta negative -> add back
            }
        }

        showRepository.save(show);

        // Auto total if absent
        if (booking.getTotalAmount() == null) {
            booking.setTotalAmount(multiply(show.getPrice(), booking.getNumberOfSeats()));
        }

        return bookingRepository.save(booking);
    }

    /**
     * Delete a booking and restore seats if it was CONFIRMED.
     */
    @Transactional
    public void deleteBooking(BookingId id) {
        Booking existing = bookingRepository.findById(id).orElseThrow(
                () -> new IllegalStateException("Booking not found.")
        );

        boolean wasConfirmed = existing.getStatus() != null && "CONFIRMED".equalsIgnoreCase(existing.getStatus().name());
        if (wasConfirmed) {
            ShowId sid = new ShowId(
                    existing.getShowsIdshows(),
                    existing.getShowsMoviesIdmovies(),
                    existing.getShowsTheatresIdtheatres());
            Show show = showRepository.findById(sid).orElse(null);
            if (show != null) {
                int seats = existing.getNumberOfSeats() == null ? 0 : existing.getNumberOfSeats();
                show.setAvailableSeats(show.getAvailableSeats() + seats);
                showRepository.save(show);
            }
        }

        bookingRepository.deleteById(id);
    }
}