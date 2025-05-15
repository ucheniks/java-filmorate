package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.DirectorRepository;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorRepository directorRepository;

    public List<Director> getAllDirectors() {
        return directorRepository.findAll();
    }

    public Director getDirectorById(Long id) {
        return directorRepository.getById(id);
    }

    public Director createDirector(Director director) {
        return directorRepository.addDirector(director);
    }

    public Director updateDirector(Director director) {
        return directorRepository.updateDirector(director);
    }

    public void deleteDirector(Long id) {
        directorRepository.deleteDirector(id);
    }

}
