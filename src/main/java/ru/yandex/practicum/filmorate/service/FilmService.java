package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.exceptions.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.dal.FilmDbStorage;
import ru.yandex.practicum.filmorate.dal.UserDbStorage;
import ru.yandex.practicum.filmorate.model.enums.EventOperation;
import ru.yandex.practicum.filmorate.model.enums.EventType;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Validated
public class FilmService {
    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;
    private final EventService eventService;

    public List<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film getFilmById(Long id) {
        return filmStorage.getFilmById(id);
    }

    public Film addFilm(Film film) {
        log.info("Добавление фильма с id {} на уровне сервиса", film.getId());
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        getFilmById(film.getId());
        return filmStorage.updateFilm(film);
    }

    public void addLike(Long filmId, Long userId) {
        Film film = getFilmById(filmId);
        userStorage.getUserById(userId);
        if (film.getLikes().contains(userId)) {
            eventService.addEvent(
                    userId,
                    EventType.LIKE,
                    EventOperation.ADD,
                    filmId
            );
            return;
        }
        filmStorage.addLike(filmId, userId);
        eventService.addEvent(
                userId,
                EventType.LIKE,
                EventOperation.ADD,
                filmId
        );
    }

    public void removeLike(Long filmId, Long userId) {
        Film film = getFilmById(filmId);
        userStorage.getUserById(userId);

        if (!film.getLikes().contains(userId)) {
            throw new NotFoundException("Лайк не найден");
        }
        filmStorage.removeLike(filmId, userId);
        eventService.addEvent(
                userId,
                EventType.LIKE,
                EventOperation.REMOVE,
                filmId
        );
    }

    public List<Film> getPopularFilms(Integer count, Long genreId, Integer year) {
        if (count != null && count <= 0) {
            throw new ParameterNotValidException("count Должно быть положительным");
        }
        return filmStorage.getPopularFilms(count, genreId, year);
    }

    public List<Film> getDirectorsFilms(Long directorId, String sortBy) {
        return filmStorage.getDirectorsFilms(directorId, sortBy);
    }

    public List<Film> getCommonFilms(Long userId, Long friendId) {
        if (userId == null || friendId == null) {
            throw new ParameterNotValidException("userId и friendId не могут быть null");
        }
        return filmStorage.getCommonFilms(userId, friendId);
    }

    public void deleteFilmById(Long id) {
        log.info("Удаления фильма с id {}", id);
        filmStorage.deleteFilmById(id);
    }

    public List<Film> searchFilms(String query, String[] by) {
        return filmStorage.searchFilms(query, by);
    }
}
