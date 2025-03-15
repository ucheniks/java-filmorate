package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public List<Film> getUsers() {
        return new ArrayList<>(films.values());
    }

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        log.info("Добавление нового фильма: {}", film);
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.error("Ошибка при добавлении фильма: дата релиза фильма {} раньше 28 декабря 1895 года", film.getName());
            throw new ValidationException("Дата релиза фильма — не раньше 28 декабря 1895 года");
        }
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Фильм успешно добавлен: {}", film);
        return film;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film newFilm) {
        log.info("Обновление фильма: {}", newFilm);
        if (!films.containsKey(newFilm.getId())) {
            log.error("Ошибка при обновлении фильма: фильм с id {} не найден", newFilm.getId());
            throw new NotFoundException("Фильм с id " + newFilm.getId() + " не найден");
        }
        if (newFilm.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.error("Ошибка при обновлении фильма: дата релиза фильма {} раньше 28 декабря 1895 года", newFilm.getName());
            throw new ValidationException("Дата релиза фильма — не раньше 28 декабря 1895 года");
        }

        films.put(newFilm.getId(), newFilm);
        log.info("Фильм успешно обновлён: {}", newFilm);
        return newFilm;

    }


    private Long getNextId() {
        long maxId = films.keySet().stream().
                mapToLong(id -> id).
                max().
                orElse(0);
        return ++maxId;
    }
}
