package ru.yandex.practicum.filmorate.dal.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.DirectorRepository;
import ru.yandex.practicum.filmorate.dal.GenreRepository;
import ru.yandex.practicum.filmorate.dal.MpaRatingRepository;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FilmRowMapper implements RowMapper<Film> {
    private static final String FIND_LIKES_QUERY = "SELECT user_id FROM likes WHERE film_id = ?";

    private final MpaRatingRepository mpaRatingRepository;
    private final GenreRepository genreRepository;
    private final DirectorRepository directorRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("film_id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));

        Long ratingId = rs.getLong("rating_id");
        film.setMpa(mpaRatingRepository.getById(ratingId));

        film.setGenres(new HashSet<>(genreRepository.findByFilmId(film.getId())));

        film.setDirectors(new HashSet<>(directorRepository.findByFilmId(film.getId())));

        List<Long> likes = jdbcTemplate.queryForList(FIND_LIKES_QUERY, Long.class, film.getId());
        film.setLikes(new HashSet<>(likes));

        return film;
    }
}