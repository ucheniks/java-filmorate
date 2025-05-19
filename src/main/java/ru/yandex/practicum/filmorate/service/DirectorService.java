package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.DirectorRepository;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorRepository directorRepository;

    public List<Director> getAllDirectors() {
        log.info("Получение списка всех режиссёров на уровне сервиса");
        return directorRepository.findAll();
    }

    public Director getDirectorById(Long id) {
        log.info("Получение режиссёра с ID {} на уровне сервиса", id);
        return directorRepository.getById(id);
    }

    public Director createDirector(Director director) {
        log.info("Создание режиссёра на уровне сервиса");
        return directorRepository.addDirector(director);
    }

    public Director updateDirector(Director director) {
        log.info("Обновление режиссёра на уровне сервиса");
        return directorRepository.updateDirector(director);
    }

    public void deleteDirector(Long id) {
        log.info("Удаление режиссёра с ID {} на уровне сервиса", id);
        directorRepository.deleteDirector(id);
    }

}
