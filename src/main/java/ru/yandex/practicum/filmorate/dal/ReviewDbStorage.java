package ru.yandex.practicum.filmorate.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.ReviewRowMapper;
import ru.yandex.practicum.filmorate.exceptions.InternalServerException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.film.ReviewStorage;

import java.util.List;

@Slf4j
@Repository
public class ReviewDbStorage extends BaseDbStorage<Review> implements ReviewStorage {

    private static final String FIND_BY_ID_QUERY = "SELECT * FROM reviews WHERE review_id = ?";
    private static final String FIND_BY_FILM_QUERY = "SELECT * FROM reviews WHERE film_id = ? ORDER BY useful DESC LIMIT ?";
    private static final String FIND_ALL_QUERY = "SELECT * FROM reviews ORDER BY useful DESC LIMIT ?";
    private static final String INSERT_REVIEW_QUERY =
            "INSERT INTO reviews (content, is_positive, user_id, film_id, useful) VALUES (?, ?, ?, ?, 0)";
    private static final String UPDATE_REVIEW_QUERY =
            "UPDATE reviews SET content = ?, is_positive = ? WHERE review_id = ?";
    private static final String DELETE_REVIEW_QUERY =
            "DELETE FROM reviews WHERE review_id = ?";

    private static final String CHECK_LIKE_QUERY =
            "SELECT COUNT(*) FROM review_likes WHERE review_id = ? AND user_id = ?";
    private static final String INSERT_LIKE_QUERY =
            "INSERT INTO review_likes (review_id, user_id, is_positive) VALUES (?, ?, ?)";
    private static final String UPDATE_LIKE_QUERY =
            "UPDATE review_likes SET is_positive = ? WHERE review_id = ? AND user_id = ?";
    private static final String DELETE_LIKE_QUERY =
            "DELETE FROM review_likes WHERE review_id = ? AND user_id = ?";
    private static final String GET_LIKE_STATUS_QUERY =
            "SELECT is_positive FROM review_likes WHERE review_id = ? AND user_id = ?";
    private static final String UPDATE_USEFUL_QUERY =
            "UPDATE reviews SET useful = useful + ? WHERE review_id = ?";

    @Autowired
    public ReviewDbStorage(JdbcTemplate jdbc, ReviewRowMapper reviewRowMapper) {
        super(jdbc, reviewRowMapper);
    }

    @Override
    public Review getReviewById(long reviewId) {
        return findOne(FIND_BY_ID_QUERY, reviewId)
                .orElseThrow(() -> new NotFoundException("Review with ID " + reviewId + " not found."));
    }

    @Override
    public List<Review> getReviewsByFilmId(long filmId, int count, boolean byFilm) {
        log.info("Получение отзывов в хранилище");
        if (byFilm) {
            return findMany(FIND_BY_FILM_QUERY, filmId, count);
        } else {
            return findMany(FIND_ALL_QUERY, count);
        }
    }

    @Override
    public Review addReview(Review review) {
        long reviewId = insert(INSERT_REVIEW_QUERY,
                review.getContent(),
                review.getIsPositive(),
                review.getUserId(),
                review.getFilmId());
        review.setReviewId(reviewId);
        return review;
    }

    @Override
    public Review updateReview(Review review) {
        update(UPDATE_REVIEW_QUERY, review.getContent(), review.getIsPositive(), review.getReviewId());
        return review;
    }

    @Override
    public void deleteReview(long reviewId) {
        update(DELETE_REVIEW_QUERY, reviewId);
    }

    @Override
    public boolean setLikeOrDislike(long reviewId, long userId, boolean isPositive) {
        try {
            log.debug("Попытка установить {} к отзыву {} пользователем {}",
                    isPositive ? "лайк" : "дизлайк", reviewId, userId);

            int count = jdbc.queryForObject(CHECK_LIKE_QUERY, Integer.class, reviewId, userId);
            if (count == 0) {
                jdbc.update(INSERT_LIKE_QUERY, reviewId, userId, isPositive);
                updateUsefulCount(reviewId, isPositive ? 1 : -1);
                log.debug("Добавлен {} для отзыва {} пользователем {}",
                        isPositive ? "лайк" : "дизлайк", reviewId, userId);
            } else {
                jdbc.update(UPDATE_LIKE_QUERY, isPositive, reviewId, userId);
                updateUsefulCount(reviewId, isPositive ? 2 : -2);
                log.debug("Обновлен {} для отзыва {} пользователем {}",
                        isPositive ? "лайк" : "дизлайк", reviewId, userId);
            }
            return true;
        } catch (InternalServerException e) {
            log.error("Ошибка при установке {} для отзыва {} пользователем {}: {}",
                    isPositive ? "лайка" : "дизлайка", reviewId, userId, e.getMessage(), e);
            throw new InternalServerException("Не удалось поставить " +
                    (isPositive ? "лайк" : "дизлайк") + " отзыву. Попробуйте позже.");
        }
    }

    private void updateUsefulCount(long reviewId, int delta) {
        jdbc.update(UPDATE_USEFUL_QUERY, delta, reviewId);
        log.debug("Рейтинг отзыва {} изменен на {}", reviewId, delta > 0 ? "+" + delta : delta);
    }

    @Override
    public boolean removeLikeOrDislike(long reviewId, long userId) {
        try {
            log.debug("Удаляем лайк/дизлайк к отзыву {} пользователем {}", reviewId, userId);
            int affectedRows = jdbc.update(DELETE_LIKE_QUERY, reviewId, userId);

            if (affectedRows > 0) {
                log.debug("Лайк/дизлайк удален. Обновляем рейтинг.");
                updateUsefulCount(reviewId, -getUsefulDelta(reviewId, userId));
            }

            return affectedRows > 0;
        } catch (InternalServerException e) {
            log.error("Ошибка при удалении лайка/дизлайка для отзыва {} пользователем {}: {}",
                    reviewId, userId, e.getMessage(), e);
            throw new InternalServerException("Не удалось удалить лайк/дизлайк отзыву. Попробуйте позже.");
        }
    }

    private int getUsefulDelta(long reviewId, long userId) {
        Boolean isPositive = jdbc.query(GET_LIKE_STATUS_QUERY,
                rs -> rs.next() ? rs.getBoolean("is_positive") : null,
                reviewId, userId);
        return (isPositive != null && isPositive) ? -1 : 1;
    }
}