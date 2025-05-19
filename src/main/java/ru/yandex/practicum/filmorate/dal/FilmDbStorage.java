package ru.yandex.practicum.filmorate.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ParameterNotValidException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.model.enums.SortBy;
import ru.yandex.practicum.filmorate.model.enums.SearchType;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class FilmDbStorage extends BaseDbStorage<Film> implements FilmStorage {
    private static final String FIND_ALL_QUERY = "SELECT * FROM films";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM films WHERE film_id = ?";
    private static final String INSERT_QUERY = "INSERT INTO films(name, description, release_date, duration, rating_id) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, rating_id = ? WHERE film_id = ?";
    private static final String ADD_LIKE_QUERY = "INSERT INTO likes(film_id, user_id) VALUES (?, ?)";
    private static final String REMOVE_LIKE_QUERY = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
    private static final String GET_POPULAR_QUERY = "SELECT f.* FROM films f LEFT JOIN likes l ON f.film_id = l.film_id GROUP BY f.film_id ORDER BY COUNT(l.user_id) DESC";
    private static final String GET_POPULAR_QUERY_GENRE = "SELECT f.* FROM films f LEFT JOIN likes l ON f.film_id = l.film_id LEFT JOIN film_genres fg ON f.film_id = fg.film_id WHERE fg.genre_id = ? GROUP BY f.film_id ORDER BY COUNT(l.user_id) DESC";
    private static final String GET_POPULAR_QUERY_YEAR = "SELECT f.* FROM films f LEFT JOIN likes l ON f.film_id = l.film_id WHERE EXTRACT(YEAR FROM CAST(release_date AS DATE)) = ? GROUP BY f.film_id ORDER BY COUNT(l.user_id) DESC";
    private static final String GET_POPULAR_QUERY_GENRE_AND_YEAR = "SELECT f.* FROM films f LEFT JOIN likes l ON f.film_id = l.film_id LEFT JOIN film_genres fg ON f.film_id = fg.film_id WHERE fg.genre_id = ? AND EXTRACT(YEAR FROM CAST(release_date AS DATE)) = ? GROUP BY f.film_id ORDER BY COUNT(l.user_id) DESC";
    private static final String GET_SEARCH_BY_TITLE_QUERY = """
            SELECT f.*
            FROM films f
            LEFT JOIN likes l ON f.film_id = l.film_id
            WHERE UPPER(f.name) LIKE UPPER(CONCAT('%', ?, '%'))
            GROUP BY f.film_id
            ORDER BY COUNT(l.user_id) DESC
            """;
    private static final String GET_SEARCH_BY_DIRECTOR_QUERY = """
            SELECT f.*
            FROM films f
            LEFT JOIN film_directors fd ON f.film_id = fd.film_id
            LEFT JOIN directors d ON fd.director_id = d.director_id
            LEFT JOIN likes l ON f.film_id = l.film_id
            WHERE UPPER(d.name) LIKE UPPER(CONCAT('%', ?, '%'))
            GROUP BY f.film_id
            ORDER BY COUNT(l.user_id) DESC
            """;
    private static final String GET_SEARCH_BY_BOTH_QUERY = """
            SELECT f.*
            FROM films f
            LEFT JOIN film_directors fd ON f.film_id = fd.film_id
            LEFT JOIN directors d ON fd.director_id = d.director_id
            LEFT JOIN likes l ON f.film_id = l.film_id
            WHERE UPPER(f.name) LIKE UPPER(CONCAT('%', ?, '%')) OR
                  UPPER(d.name) LIKE UPPER(CONCAT('%', ?, '%'))
            GROUP BY f.film_id
            ORDER BY COUNT(l.user_id) DESC
            """;
    private static final String GET_LIKED_FILMS = "SELECT film_id FROM likes WHERE user_id = ?";
    private static final String GET_SIMILAR_USER = "SELECT user_id FROM likes WHERE film_id IN (SELECT film_id FROM likes WHERE user_id = ?) AND user_id != ? GROUP BY user_id ORDER BY COUNT(film_id) DESC LIMIT 1";
    private static final String GET_SIMILAR_USER_LIKED_FILMS = "SELECT  f.* FROM films f JOIN likes l ON f.film_id = l.film_id WHERE user_id IN (" + GET_SIMILAR_USER + ")";
    private static final String GET_FILM_RECOMMENDATIONS = GET_SIMILAR_USER_LIKED_FILMS + "AND l.film_id NOT IN (" + GET_LIKED_FILMS + ")";
    private static final String GET_DIRECTORS_FILMS_BY_LIKES = """
            SELECT f.*
            FROM films f
            JOIN film_directors fd ON f.film_id = fd.film_id
            LEFT JOIN likes l ON f.film_id = l.film_id
            WHERE fd.director_id = ?
            GROUP BY f.film_id
            ORDER BY COUNT(l.user_id) DESC
            """;
    private static final String GET_DIRECTORS_FILMS_BY_YEAR = """
            SELECT f.*
            FROM films f
            JOIN film_directors fd ON f.film_id = fd.film_id
            WHERE fd.director_id = ?
            GROUP BY f.film_id
            ORDER BY f.release_date
            """;
    private static final String GET_COMMON_QUERY = "SELECT f.* FROM films f JOIN likes l1 ON f.film_id = l1.film_id AND l1.user_id = ? JOIN likes l2 ON f.film_id = l2.film_id AND l2.user_id = ?";
    private static final String REMOVE_FILM_BY_ID_QUERY = """
            DELETE FROM films
            WHERE film_id = ?
            """;

    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);


    private final JdbcTemplate jdbc;
    private final FilmRowMapper filmRowMapper;
    private final MpaRatingRepository mpaRatingRepository;
    private final GenreRepository genreRepository;
    private final DirectorRepository directorRepository;

    public FilmDbStorage(JdbcTemplate jdbc,
                         FilmRowMapper filmRowMapper,
                         MpaRatingRepository mpaRatingRepository,
                         GenreRepository genreRepository,
                         DirectorRepository directorRepository) {
        super(jdbc, filmRowMapper);
        this.jdbc = jdbc;
        this.filmRowMapper = filmRowMapper;
        this.mpaRatingRepository = mpaRatingRepository;
        this.genreRepository = genreRepository;
        this.directorRepository = directorRepository;
    }

    @Override
    public List<Film> getFilms() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Film getFilmById(Long id) {
        return findOne(FIND_BY_ID_QUERY, id)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + id + " не найден"));
    }

    @Override
    public Film addFilm(Film film) {
        validateFilmReleaseDate(film);
        log.info("Добавление фильма с id {} на уровне репозитория 1", film.getId());
        film = setMpaAndGenresAndDirectorsToFilm(film);
        log.info("Добавление фильма с id {} на уровне репозитория 2", film.getId());
        long id = insert(INSERT_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId());
        film.setId(id);
        addGenresAndDirectorsToDb(film);
        log.info("Добавление жанров к фильму с id {} на уровне репозитория", film.getId());
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        validateFilmReleaseDate(film);
        film = setMpaAndGenresAndDirectorsToFilm(film);
        update(UPDATE_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());
        addGenresAndDirectorsToDb(film);
        return film;
    }

    public void addLike(Long filmId, Long userId) {
        jdbc.update(ADD_LIKE_QUERY, filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        jdbc.update(REMOVE_LIKE_QUERY, filmId, userId);
    }

    public List<Film> getPopularFilms(Integer count, Long genreId, Integer year) {
        String query;
        if (genreId != null && year != null) {
            query = GET_POPULAR_QUERY_GENRE_AND_YEAR;
            query += limiter(count);
            return jdbc.query(query, filmRowMapper, genreId, year);
        } else if (genreId != null) {
            query = GET_POPULAR_QUERY_GENRE;
            query += limiter(count);
            return jdbc.query(query, filmRowMapper, genreId);
        } else if (year != null) {
            query = GET_POPULAR_QUERY_YEAR;
            query += limiter(count);
            return jdbc.query(query, filmRowMapper, year);
        } else {
            query = GET_POPULAR_QUERY;
            query += limiter(count);
            return jdbc.query(query, filmRowMapper);
        }
    }

    private String limiter(Integer count) {
        String limiter = "";
        if (count != null) {
            limiter = " LIMIT " + count.toString();
        }
        return limiter;
    }

    public List<Film> getRecommendations(Long userId) {
        return jdbc.query(GET_FILM_RECOMMENDATIONS, mapper, userId, userId, userId);
    }

    public List<Film> getDirectorsFilms(Long directorId, String sortBy) {
        directorRepository.getById(directorId);
        return switch (SortBy.valueOf(sortBy.toUpperCase())) {
            case SortBy.LIKES -> jdbc.query(GET_DIRECTORS_FILMS_BY_LIKES, filmRowMapper, directorId);
            case SortBy.YEAR -> jdbc.query(GET_DIRECTORS_FILMS_BY_YEAR, filmRowMapper, directorId);
            default -> throw new ParameterNotValidException("Параметр сортировки может быть только: likes, year");
        };
    }

    public List<Film> getCommonFilms(Long userId, Long friendId) {
        return (jdbc.query(GET_COMMON_QUERY, filmRowMapper, userId, friendId));
    }

    public void deleteFilmById(Long id) {
        getFilmById(id);
        update(REMOVE_FILM_BY_ID_QUERY, id);
    }

    public List<Film> searchFilms(String query, String[] by) {
        SearchType searchType = checkSearchParams(by);
        return switch (searchType) {
            case SearchType.BOTH -> jdbc.query(GET_SEARCH_BY_BOTH_QUERY, filmRowMapper, query, query);
            case SearchType.TITLE -> jdbc.query(GET_SEARCH_BY_TITLE_QUERY, filmRowMapper, query);
            case SearchType.DIRECTOR -> jdbc.query(GET_SEARCH_BY_DIRECTOR_QUERY, filmRowMapper, query);
            default -> throw new ValidationException("Ошибка при определении типа поиска");
        };
    }

    private Film setMpaAndGenresAndDirectorsToFilm(Film film) {
        Long mpaId = film.getMpa().getId();
        film.setMpa(mpaRatingRepository.getById(mpaId));

        List<Long> genreIds = film.getGenres().stream()
                .map(Genre::getId)
                .toList();
        validateGenres(genreIds);
        Set<Genre> genres = genreRepository.findByIds(genreIds);
        film.setGenres(genres);

        List<Long> directorsIds = film.getDirectors().stream()
                .map(Director::getId)
                .toList();
        Set<Director> directors = directorRepository.findByIds(directorsIds);
        film.setDirectors(directors);

        return film;
    }

    private void addGenresAndDirectorsToDb(Film film) {
        List<Long> genreIds = new ArrayList<>(film.getGenres().stream()
                .map(Genre::getId)
                .toList());
        genreRepository.removeAllGenresFromFilm(film.getId());
        genreRepository.addGenresToFilm(film.getId(), genreIds);

        List<Long> directorsIds = new ArrayList<>(film.getDirectors().stream()
                .map(Director::getId)
                .toList());
        directorRepository.removeAllDirectorsFromFilm(film.getId());
        directorRepository.addDirectorsToFilm(film.getId(), directorsIds);

    }

    private void validateFilmReleaseDate(Film film) {
        if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            log.error("Ошибка при валидации фильма: дата релиза фильма {} раньше {}", film.getName(), MIN_RELEASE_DATE);
            throw new ValidationException("Дата релиза фильма — не раньше " + MIN_RELEASE_DATE);
        }
    }

    private void validateGenres(List<Long> genreIds) {
        boolean isValid = genreIds.stream()
                .allMatch(id -> id >= 1 && id <= 6);

        if (!isValid) {
            throw new NotFoundException("Id жанров должны быть в диапазоне от 1 до 6");
        }
    }

    private SearchType checkSearchParams(String[] by) {
        if (by == null || by.length == 0 || by.length > 2) {
            log.error("Недопустимое количество параметров by: {}", by != null ? by.length : "null");
            throw new ParameterNotValidException("Параметр by должен содержать 1 или 2 значения (title/director)");
        }

        Set<String> params = Arrays.stream(by)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        if (params.size() == 2 && params.contains("title") && params.contains("director")) {
            return SearchType.BOTH;
        }
        if (params.size() == 1) {
            if (params.contains("title")) return SearchType.TITLE;
            if (params.contains("director")) return SearchType.DIRECTOR;
        }

        log.error("Недопустимые значения параметра by: {}", Arrays.toString(by));
        throw new ParameterNotValidException(
                "Параметр by должен содержать только 'title' и/или 'director'"
        );
    }
}
