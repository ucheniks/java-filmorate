package ru.yandex.practicum.filmorate.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.*;
import java.util.stream.Stream;

@Slf4j
@Repository
public class DirectorRepository extends BaseDbStorage<Director> {
    private static final String FIND_ALL_QUERY = "SELECT * FROM directors ORDER BY director_id";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM directors WHERE director_id = ?";
    private static final String INSERT_QUERY = """
            INSERT INTO directors(name)
            VALUES (?)""";
    private static final String UPDATE_QUERY = """
            UPDATE directors
            SET name = ?
            WHERE director_id = ?""";
    private static final String FIND_BY_FILM_QUERY = """
            SELECT d.*
            FROM directors d
            JOIN film_directors fd ON d.director_id = fd.director_id
            WHERE fd.film_id = ?
            ORDER BY d.director_id""";
    private static final String DELETE_QUERY = "DELETE FROM directors WHERE director_id = ?";
    private static final String FIND_BY_IDS_QUERY = "SELECT * FROM directors WHERE director_id IN (%s)";
    private static final String INSERT_DIRECTORS_QUERY = "INSERT INTO film_directors(film_id, director_id) VALUES %s";
    private static final String DELETE_DIRECTORS_QUERY = "DELETE FROM film_directors WHERE film_id = ?";


    private final JdbcTemplate jdbc;

    public DirectorRepository(JdbcTemplate jdbc, RowMapper<Director> mapper) {
        super(jdbc, mapper);
        this.jdbc = jdbc;
    }

    public List<Director> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    public Optional<Director> findById(Long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    public Director getById(Long id) {
        log.info("Получение режиссёра");
        return findById(id).orElseThrow(() ->
                new NotFoundException("Режиссёр  с id " + id + " не найден"));
    }

    public Director addDirector(Director director) {
        long id = insert(INSERT_QUERY, director.getName());
        director.setId(id);

        return director;
    }

    public Director updateDirector(Director director) {
        getById(director.getId());
        update(UPDATE_QUERY, director.getName(), director.getId());

        return director;
    }

    public void deleteDirector(Long id) {
        getById(id);
        update(DELETE_QUERY, id);
    }

    public Set<Director> findByIds(List<Long> ids) {
        if (ids.isEmpty()) {
            return new HashSet<>();
        }

        String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));
        String query = String.format(FIND_BY_IDS_QUERY, placeholders);

        Object[] params = ids.toArray();

        List<Director> directors = findMany(query, params);

        if (directors.size() != ids.size()) {
            throw new NotFoundException("Некоторые режиссеры не найдены в базе");
        }

        return new LinkedHashSet<>(findMany(query, params));
    }

    public void addDirectorsToFilm(Long filmId, List<Long> ids) {
        if (ids.isEmpty()) {
            return;
        }

        String placeholders = String.join(",", Collections.nCopies(ids.size(), "(?, ?)"));
        String query = String.format(INSERT_DIRECTORS_QUERY, placeholders);

        Object[] params = ids.stream()
                .flatMap(directorId -> Stream.of(filmId, directorId))
                .toArray();

        log.info("Добавление директоров {} к фильму {}", ids, filmId);
        jdbc.update(query, params);
        log.info("Директоры успешно добавлены");
    }

    public List<Director> findByFilmId(Long filmId) {
        return findMany(FIND_BY_FILM_QUERY, filmId);
    }

    public void removeAllDirectorsFromFilm(Long filmId) {
        jdbc.update(DELETE_DIRECTORS_QUERY, filmId);
    }

}
