package ru.yandex.practicum.filmorate.dal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dal.mappers.DirectorRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.MpaRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, FilmRowMapper.class, MpaRatingRepository.class, GenreRepository.class, MpaRowMapper.class, GenreRowMapper.class, DirectorRepository.class, DirectorRowMapper.class})
class FilmDbStorageTest {

    @Autowired
    private FilmDbStorage filmStorage;

    private Film testFilm;

    @BeforeEach
    void setUp() {
        testFilm = new Film();
        testFilm.setName("Test Film");
        testFilm.setDescription("Test Description");
        testFilm.setReleaseDate(LocalDate.of(2005, 6, 19));
        testFilm.setDuration(120);
        testFilm.setMpa(new MpaRating(1L, null));
    }

    @Test
    void addAndFindFilmById() {
        Film addedFilm = filmStorage.addFilm(testFilm);
        Film foundFilm = filmStorage.getFilmById(addedFilm.getId());

        assertThat(foundFilm)
                .isNotNull()
                .hasFieldOrPropertyWithValue("name", "Test Film")
                .hasFieldOrPropertyWithValue("duration", 120);
    }

    @Test
    void updateFilm() {
        Film addedFilm = filmStorage.addFilm(testFilm);
        addedFilm.setDescription("Updated Description");

        filmStorage.updateFilm(addedFilm);
        Film updatedFilm = filmStorage.getFilmById(addedFilm.getId());

        assertThat(updatedFilm.getDescription()).isEqualTo("Updated Description");
    }

    @Test
    void getPopularFilms() {
        Film film1 = filmStorage.addFilm(testFilm);

        Film film2 = new Film();
        film2.setName("Another Film");
        film2.setDescription("Another Description");
        film2.setReleaseDate(LocalDate.of(2005, 7, 19));
        film2.setDuration(90);
        film2.setMpa(new MpaRating(2L, null));
        film2 = filmStorage.addFilm(film2);

        List<Film> popularFilms = filmStorage.getPopularFilms(2, null, null);

        assertThat(popularFilms).hasSize(2);
    }
}