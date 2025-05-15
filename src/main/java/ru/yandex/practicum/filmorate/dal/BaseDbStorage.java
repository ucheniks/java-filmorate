package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import ru.yandex.practicum.filmorate.exceptions.InternalServerException;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class BaseDbStorage<T> {
    protected final JdbcTemplate jdbc;
    protected final RowMapper<T> mapper;

    protected Optional<T> findOne(String query, Object... params) {
        log.debug("Выполнение запроса поиска одного: {} с параметрами: {}", query, params);
        try {
            T result = jdbc.queryForObject(query, mapper, params);
            log.debug("Найдена запись: {}", result);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException e) {
            log.debug("Запись не найдена для запроса: {}", query);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Ошибка при выполнении запроса поиска одного {}: {}", query, e.getMessage());
            throw e;
        }
    }

    protected List<T> findMany(String query, Object... params) {
        log.debug("Выполнение запроса: {} с параметрами: {}", query, params);
        try {
            List<T> results = jdbc.query(query, mapper, params);
            log.debug("Найдено {} записей", results.size());
            return results;
        } catch (Exception e) {
            log.error("Ошибка при выполнении запроса {}: {}", query, e.getMessage());
            throw e;
        }
    }

    protected void update(String query, Object... params) {
        log.debug("Выполнение обновления/удаления: {} с параметрами: {}", query, params);
        int rowsUpdated = jdbc.update(query, params);
        if (rowsUpdated == 0) {
            log.error("Не удалось обновить/удалить данные. Запрос: {}, параметры: {}", query, params);
            throw new InternalServerException("Не удалось обновить/удалить данные");
        }
        log.debug("Обновлено/удалено {} строк", rowsUpdated);
    }

    protected long insert(String query, Object... params) {
        log.debug("Выполнение вставки: {} с параметрами: {}", query, params);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            jdbc.update(connection -> {
                PreparedStatement ps = connection
                        .prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                for (int idx = 0; idx < params.length; idx++) {
                    ps.setObject(idx + 1, params[idx]);
                }
                return ps;
            }, keyHolder);

            Long id = keyHolder.getKeyAs(Long.class);

            if (id != null) {
                log.debug("Создана новая запись с ID: {}", id);
                return id;
            } else {
                log.error("Не удалось получить ID после вставки. Запрос: {}", query);
                throw new InternalServerException("Не удалось сохранить данные");
            }
        } catch (Exception e) {
            log.error("Ошибка при выполнении вставки {}: {}", query, e.getMessage());
            throw e;
        }
    }
}