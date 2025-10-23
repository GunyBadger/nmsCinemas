package com.nmsCinemas.models;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite PK for bookings:
 *  idbookings (auto-increment)
 *  showsIdshows
 *  showsMoviesIdmovies
 *  showsTheatresIdtheatres
 *  usersIdusers
 *
 * This class is used with @IdClass in Booking.
 */
public class BookingId implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idbookings;
    private Long showsIdshows;
    private Long showsMoviesIdmovies;
    private Long showsTheatresIdtheatres;
    private Long usersIdusers;

    public BookingId() {}

    public BookingId(Long idbookings,
                     Long showsIdshows,
                     Long showsMoviesIdmovies,
                     Long showsTheatresIdtheatres,
                     Long usersIdusers) {
        this.idbookings = idbookings;
        this.showsIdshows = showsIdshows;
        this.showsMoviesIdmovies = showsMoviesIdmovies;
        this.showsTheatresIdtheatres = showsTheatresIdtheatres;
        this.usersIdusers = usersIdusers;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BookingId)) return false;
        BookingId that = (BookingId) o;
        return Objects.equals(idbookings, that.idbookings)
            && Objects.equals(showsIdshows, that.showsIdshows)
            && Objects.equals(showsMoviesIdmovies, that.showsMoviesIdmovies)
            && Objects.equals(showsTheatresIdtheatres, that.showsTheatresIdtheatres)
            && Objects.equals(usersIdusers, that.usersIdusers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idbookings, showsIdshows, showsMoviesIdmovies, showsTheatresIdtheatres, usersIdusers);
    }
}
