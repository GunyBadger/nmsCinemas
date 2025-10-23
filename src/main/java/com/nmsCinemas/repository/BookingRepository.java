package com.nmsCinemas.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.nmsCinemas.models.Booking;
import com.nmsCinemas.models.BookingId;

public interface BookingRepository extends JpaRepository<Booking, BookingId> {

    // Used by /api/users/{id}/bookings endpoint
    List<Booking> findByUsersIdusers(Long usersIdusers);

    // Used by BookingRestController
    List<Booking> findByShowsIdshowsAndShowsMoviesIdmoviesAndShowsTheatresIdtheatres(
        Long showsIdshows,
        Long showsMoviesIdmovies,
        Long showsTheatresIdtheatres
    );

    // Manual id generator helper for composite PK
    @Query("select coalesce(max(b.idbookings), 0) from Booking b")
    Long findMaxBookingId();

    // ✅ ADD THIS - Eagerly fetch show, user, movie, and theatre
    @Query("SELECT b FROM Booking b " +
           "LEFT JOIN FETCH b.show s " +
           "LEFT JOIN FETCH b.user " +
           "LEFT JOIN FETCH s.movie " +
           "LEFT JOIN FETCH s.theatre")
    List<Booking> findAllWithDetails();
}