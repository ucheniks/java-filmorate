package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.dal.MpaRatingRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MpaRatingService {
    private final MpaRatingRepository mpaRatingRepository;

    public List<MpaRating> getAllMpaRatings() {
        log.info("Получение списка всех рейтингов MPA на уровне сервиса");
        return mpaRatingRepository.findAll();
    }

    public MpaRating getMpaRatingById(Long id) {
        log.info("Получение рейтинга MPA с ID: {} на уровне сервиса", id);
        return mpaRatingRepository.getById(id);
    }
}