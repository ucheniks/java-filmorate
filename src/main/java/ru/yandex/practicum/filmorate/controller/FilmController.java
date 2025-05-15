package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;
    private static final String DEFAULT_DIRECTORS_SORT_TYPE = "likes";

    @GetMapping
    public List<Film> getAllFilms() {
        log.info("Получение списка всех фильмов");
        return filmService.getFilms();
    }

    @GetMapping("/{id}")
    public Film getFilm(@PathVariable Long id) {
        log.info("Получение фильма с ID: {}", id);
        return filmService.getFilmById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film createFilm(@Valid @RequestBody Film film) {
        log.info("Добавление фильма с id {} на уровне контроллера", film.getId());
        return filmService.addFilm(film);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        return filmService.updateFilm(film);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(
            @PathVariable Long id,
            @PathVariable Long userId) {
        log.info("Добавление лайка фильму {} от пользователя {}", id, userId);
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(
            @PathVariable Long id,
            @PathVariable Long userId) {
        log.info("Удаление лайка у фильма {} от пользователя {}", id, userId);
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(
            @RequestParam(required = false) Integer count,
            @RequestParam(required = false) Long genreId,
            @RequestParam(required = false) Integer year) {
        if (count != null) {
            log.info("Получение топ-{} популярных фильмов", count);
        } else {
            log.info("Получение популярных фильмов");
        }
        if (genreId != null) {
            log.info("С фильтрацией по жанру с id {}", genreId);
        }
        if (year != null) {
            log.info("С фильтрацией за {} год", year);
        }
        return filmService.getPopularFilms(count, genreId, year);
    }

    @GetMapping("/director/{directorId}")
    private List<Film> getDirectorsFilms(
            @PathVariable Long directorId,
            @RequestParam(defaultValue = DEFAULT_DIRECTORS_SORT_TYPE) String sortBy) {
        log.info("Получение фильмов режиссёра, отсортированных по {}", sortBy);
        return filmService.getDirectorsFilms(directorId, sortBy);
    }

    @GetMapping("/common")
    public List<Film> getCommonFilms(
            @RequestParam Long userId,
            @RequestParam Long friendId) {
        log.info("Получение общих фильмов у пользователей с id {} и {}", userId, friendId);
        return filmService.getCommonFilms(userId, friendId);
    }

    @DeleteMapping("/{filmId}")
    public void deleteFilmById(@PathVariable Long filmId) {
        log.info("Удаление фильма с id {}", filmId);
        filmService.deleteFilmById(filmId);
    }

    @GetMapping("/search")
    public List<Film> searchFilms(
            @RequestParam String query,
            @RequestParam(defaultValue = "title,director") String[] by) {
        log.info("Получение фильмов с подстрокой {} в {}", query, by);
        return filmService.searchFilms(query, by);
    }
}
