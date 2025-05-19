package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.dal.FilmDbStorage;
import ru.yandex.practicum.filmorate.dal.UserDbStorage;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.enums.EventOperation;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.storage.film.ReviewStorage;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReviewService {
    private final ReviewStorage reviewStorage;
    private final FilmDbStorage filmDbStorage;
    private final UserDbStorage userDbStorage;
    private final EventService eventService;

    @Transactional
    public Review addReview(Review review) {
        log.info("Добавление отзыва на уровне сервиса");
        validateUserAndFilmExist(review.getUserId(), review.getFilmId());
        Review newReview = reviewStorage.addReview(review);
        eventService.addEvent(
                newReview.getUserId(),
                EventType.REVIEW,
                EventOperation.ADD,
                newReview.getReviewId()
        );

        return newReview;
    }

    @Transactional
    public Review updateReview(Review review) {
        log.info("Обновление отзыва на уровне сервиса");
        Review existingReview = getReviewById(review.getReviewId());
        existingReview.setContent(review.getContent());
        existingReview.setIsPositive(review.getIsPositive());

        Review updatedReview = reviewStorage.updateReview(existingReview);
        eventService.addEvent(
                updatedReview.getUserId(),
                EventType.REVIEW,
                EventOperation.UPDATE,
                updatedReview.getReviewId()
        );
        return updatedReview;
    }

    @Transactional
    public void deleteReview(long reviewId) {
        log.info("Удаление отзыва с ID {} на уровне сервиса", reviewId);
        Review review = getReviewById(reviewId);
        reviewStorage.deleteReview(reviewId);
        eventService.addEvent(
                review.getUserId(),
                EventType.REVIEW,
                EventOperation.REMOVE,
                reviewId
        );
    }

    public Review getReviewById(long reviewId) {
        log.info("Получения отзыва с ID {} на уровне сервиса", reviewId);
        return reviewStorage.getReviewById(reviewId);
    }

    public List<Review> getReviewsByFilmId(Long filmId, int count) {
        log.info("Получение отзывов на уровне сервиса");
        boolean byFilm = true;
        if (filmId == null) {
            filmId = -1L;
            byFilm = false;
        }
        return reviewStorage.getReviewsByFilmId(filmId, count, byFilm);
    }

    @Transactional
    public void setLikeOrDislike(long reviewId, long userId, boolean isPositive) {
        log.info("Лайк на отзыв с ID {} пользователем с ID {} на уровне сервиса", reviewId, userId);
        userDbStorage.getUserById(userId);
        reviewStorage.getReviewById(reviewId);

        boolean success = reviewStorage.setLikeOrDislike(reviewId, userId, isPositive);
        if (!success) {
            throw new IllegalArgumentException("User has already " + (isPositive ? "liked" : "disliked") + " this review.");
        }
    }

    @Transactional
    public void removeLikeOrDislike(long reviewId, long userId) {
        log.info("Попытка удалить лайк/дизлайк к отзыву {} пользователем {} на уровне сервиса", reviewId, userId);
        userDbStorage.getUserById(userId);
        reviewStorage.getReviewById(reviewId);

        boolean success = reviewStorage.removeLikeOrDislike(reviewId, userId);
        if (!success) {
            throw new IllegalArgumentException("User has not liked or disliked this review.");
        }
    }


    private void validateUserAndFilmExist(long userId, long filmId) {
        userDbStorage.getUserById(userId);
        filmDbStorage.getFilmById(filmId);
    }

}