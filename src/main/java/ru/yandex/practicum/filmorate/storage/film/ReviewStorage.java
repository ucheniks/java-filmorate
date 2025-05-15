package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;


public interface ReviewStorage {
    Review addReview(Review review);

    Review updateReview(Review review);

    void deleteReview(long reviewId);

    Review getReviewById(long reviewId);

    List<Review> getReviewsByFilmId(long filmId, int count, boolean byFilm);

    boolean setLikeOrDislike(long reviewId, long userId, boolean isPositive);

    boolean removeLikeOrDislike(long reviewId, long userId);
}