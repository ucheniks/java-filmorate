package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public Review addReview(@Valid @RequestBody Review review) {
        log.info("Добавление отзыва");
        return reviewService.addReview(review);
    }

    @PutMapping
    public Review updateReview(@Valid @RequestBody Review review) {
        log.info("Обновление отзыва");
        return reviewService.updateReview(review);
    }

    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable @NotNull Long id) {
        log.info("Удаление отзыва с ID {}", id);
        reviewService.deleteReview(id);
    }

    @GetMapping("/{id}")
    public Review getReviewById(@PathVariable @NotNull Long id) {
        log.info("Получения отзыва с ID {}", id);
        return reviewService.getReviewById(id);
    }

    @GetMapping
    public List<Review> getReviews(
            @RequestParam(required = false) Long filmId,
            @RequestParam(defaultValue = "10") int count) {
        log.info("Получение отзывов");
        return reviewService.getReviewsByFilmId(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    public void likeReview(@PathVariable @NotNull Long id, @PathVariable @NotNull Long userId) {
        log.info("Лайк на отзыв с ID {} пользователем с ID {}", id, userId);
        reviewService.setLikeOrDislike(id, userId, true);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void dislikeReview(@PathVariable @NotNull Long id, @PathVariable @NotNull Long userId) {
        log.info("Дизлайк на отзыв с ID {} пользователем с ID {}", id, userId);
        reviewService.setLikeOrDislike(id, userId, false);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLikeOrDislike(@PathVariable @NotNull Long id, @PathVariable @NotNull Long userId) {
        log.info("Удаление оценки на отзыв с ID {} пользователем с ID {}", id, userId);
        reviewService.removeLikeOrDislike(id, userId);
    }

}