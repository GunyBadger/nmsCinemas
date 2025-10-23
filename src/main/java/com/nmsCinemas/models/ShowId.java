package com.nmsCinemas.models;

import java.io.Serializable;
import java.util.Objects;
import jakarta.persistence.Embeddable;

/**
 * Composite key for the shows table:
 *  - idshows (auto-increment)
 *  - moviesIdmovies (FK to movies.idmovies)
 *  - theatresIdtheatres (FK to theatres.idtheatres)
 */
@Embeddable
public class ShowId implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idshows;
    private Long moviesIdmovies;
    private Long theatresIdtheatres;

    public ShowId() {}

    public ShowId(Long idshows, Long moviesIdmovies, Long theatresIdtheatres) {
        this.idshows = idshows;
        this.moviesIdmovies = moviesIdmovies;
        this.theatresIdtheatres = theatresIdtheatres;
    }

    public Long getIdshows() { return idshows; }
    public void setIdshows(Long idshows) { this.idshows = idshows; }

    public Long getMoviesIdmovies() { return moviesIdmovies; }
    public void setMoviesIdmovies(Long moviesIdmovies) { this.moviesIdmovies = moviesIdmovies; }

    public Long getTheatresIdtheatres() { return theatresIdtheatres; }
    public void setTheatresIdtheatres(Long theatresIdtheatres) { this.theatresIdtheatres = theatresIdtheatres; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShowId)) return false;
        ShowId that = (ShowId) o;
        return Objects.equals(idshows, that.idshows)
            && Objects.equals(moviesIdmovies, that.moviesIdmovies)
            && Objects.equals(theatresIdtheatres, that.theatresIdtheatres);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idshows, moviesIdmovies, theatresIdtheatres);
    }
}
