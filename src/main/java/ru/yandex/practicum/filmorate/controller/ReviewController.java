package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public Review addReview(@Valid @RequestBody Review review) {
        return reviewService.addReview(review);
    }

    @PutMapping
    public Review updateReview(@Valid @RequestBody Review review) {
        return reviewService.updateReview(review);
    }

    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable @NotNull Long id) {
        reviewService.deleteReview(id);
    }

    @GetMapping("/{id}")
    public Review getReviewById(@PathVariable @NotNull Long id) {
        return reviewService.getReviewById(id);
    }

    @GetMapping
    public List<Review> getReviews(
            @RequestParam(required = false) Long filmId,
            @RequestParam(defaultValue = "10") int count) {
        log.info("Получение отзывов в контроллере");
        return reviewService.getReviewsByFilmId(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    public void likeReview(@PathVariable @NotNull Long id, @PathVariable @NotNull Long userId) {
        reviewService.setLikeOrDislike(id, userId, true);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void dislikeReview(@PathVariable @NotNull Long id, @PathVariable @NotNull Long userId) {
        reviewService.setLikeOrDislike(id, userId, false);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLikeOrDislike(@PathVariable @NotNull Long id, @PathVariable @NotNull Long userId) {
        reviewService.removeLikeOrDislike(id, userId);
    }

}