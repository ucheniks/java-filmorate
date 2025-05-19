package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.dal.GenreRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreRepository genreRepository;

    public List<Genre> getAllGenres() {
        log.info("Получение списка всех жанров на уровне сервиса");
        return genreRepository.findAll();
    }

    public Genre getGenreById(Long id) {
        log.info("Получение жанра с ID: {} на уровне сервиса", id);
        return genreRepository.getById(id);
    }
}