package com.nmsCinemas.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/**
 * Maps to nmsdb.bookings
 * PK: (idbookings, shows_idshows, shows_movies_idmovies, shows_theatres_idtheatres, users_idusers)
 * FKs:
 *  - (shows_idshows, shows_movies_idmovies, shows_theatres_idtheatres) -> shows
 *  - users_idusers -> users
 */
@Entity
@Table(name = "bookings")
@IdClass(BookingId.class)
public class Booking {

    // === Primary Key Parts ===
	@Id
	@Column(name = "idbookings")
	private Long idbookings;

    @Id
    @Column(name = "shows_idshows", nullable = false)
    private Long showsIdshows;

    @Id
    @Column(name = "shows_movies_idmovies", nullable = false)
    private Long showsMoviesIdmovies;

    @Id
    @Column(name = "shows_theatres_idtheatres", nullable = false)
    private Long showsTheatresIdtheatres;

    @Id
    @Column(name = "users_idusers", nullable = false)
    private Long usersIdusers;

    // === Columns ===
    @Size(max = 255, message = "Seat numbers too long.")
    @Column(name = "seat_numbers", length = 255)
    private String seatNumbers;

    @NotNull(message = "Number of seats is required.")
    @Min(value = 1, message = "At least 1 seat must be booked.")
    @Column(name = "number_of_seats", nullable = false)
    private Integer numberOfSeats;

    @Column(name = "booking_date")
    private LocalDateTime bookingDate; // defaulted at persist if null

    @DecimalMin(value = "0.00", inclusive = true, message = "Total must be >= 0.00.")
    @Digits(integer = 8, fraction = 2, message = "Total must be a valid amount with up to 2 decimals.")
    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;

    public enum Status { CONFIRMED, CANCELED }

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status = Status.CONFIRMED;

    // === Relationships (read-only joins for display/use) ===
    // Join to Show via composite foreign key
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "shows_idshows", referencedColumnName = "idshows", insertable = false, updatable = false),
        @JoinColumn(name = "shows_movies_idmovies", referencedColumnName = "movies_idmovies", insertable = false, updatable = false),
        @JoinColumn(name = "shows_theatres_idtheatres", referencedColumnName = "theatres_idtheatres", insertable = false, updatable = false)
    })
    private Show show;

    // Join to User (single FK)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_idusers", referencedColumnName = "idusers", insertable = false, updatable = false)
    private User user;

    // === Constructors ===
    public Booking() {}

    public Booking(Long idbookings,
                   Long showsIdshows,
                   Long showsMoviesIdmovies,
                   Long showsTheatresIdtheatres,
                   Long usersIdusers,
                   String seatNumbers,
                   Integer numberOfSeats,
                   LocalDateTime bookingDate,
                   BigDecimal totalAmount,
                   Status status) {
        this.idbookings = idbookings;
        this.showsIdshows = showsIdshows;
        this.showsMoviesIdmovies = showsMoviesIdmovies;
        this.showsTheatresIdtheatres = showsTheatresIdtheatres;
        this.usersIdusers = usersIdusers;
        this.seatNumbers = seatNumbers;
        this.numberOfSeats = numberOfSeats;
        this.bookingDate = bookingDate;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    // === Lifecycle ===
    @PrePersist
    protected void onCreate() {
        if (this.bookingDate == null) {
            this.bookingDate = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = Status.CONFIRMED;
        }
    }

    // === Getters/Setters ===
    public Long getIdbookings() { return idbookings; }
    public void setIdbookings(Long idbookings) { this.idbookings = idbookings; }

    public Long getShowsIdshows() { return showsIdshows; }
    public void setShowsIdshows(Long showsIdshows) { this.showsIdshows = showsIdshows; }

    public Long getShowsMoviesIdmovies() { return showsMoviesIdmovies; }
    public void setShowsMoviesIdmovies(Long showsMoviesIdmovies) { this.showsMoviesIdmovies = showsMoviesIdmovies; }

    public Long getShowsTheatresIdtheatres() { return showsTheatresIdtheatres; }
    public void setShowsTheatresIdtheatres(Long showsTheatresIdtheatres) { this.showsTheatresIdtheatres = showsTheatresIdtheatres; }

    public Long getUsersIdusers() { return usersIdusers; }
    public void setUsersIdusers(Long usersIdusers) { this.usersIdusers = usersIdusers; }

    public String getSeatNumbers() { return seatNumbers; }
    public void setSeatNumbers(String seatNumbers) { this.seatNumbers = seatNumbers; }

    public Integer getNumberOfSeats() { return numberOfSeats; }
    public void setNumberOfSeats(Integer numberOfSeats) { this.numberOfSeats = numberOfSeats; }

    public LocalDateTime getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDateTime bookingDate) { this.bookingDate = bookingDate; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public Show getShow() { return show; }
    public User getUser() { return user; }
}
