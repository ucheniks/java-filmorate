package ru.yandex.practicum.filmorate.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
public class UserDbStorage extends BaseDbStorage<User> implements UserStorage {
    private static final String FIND_ALL_QUERY = "SELECT * FROM users";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM users WHERE user_id = ?";
    private static final String FIND_BY_EMAIL_QUERY = "SELECT * FROM users WHERE email = ?";
    private static final String INSERT_QUERY = """
            INSERT INTO users(email, login, name, birthday)
            VALUES (?, ?, ?, ?)""";
    private static final String UPDATE_QUERY = """
            UPDATE users
            SET email = ?, login = ?, name = ?, birthday = ?
            WHERE user_id = ?""";
    private static final String ADD_FRIEND_QUERY = """
            MERGE INTO friends(user_id, friend_id, status)
            KEY(user_id, friend_id)
            VALUES (?, ?, ?)""";
    private static final String CONFIRM_FRIEND_QUERY = """
            UPDATE friends
            SET status = 'CONFIRMED'
            WHERE user_id = ? AND friend_id = ?""";
    private static final String REMOVE_FRIEND_QUERY = """
            DELETE FROM friends
            WHERE (user_id = ? AND friend_id = ?)""";
    private static final String GET_FRIENDS_QUERY = """
            SELECT u.* , f.status FROM users u
            JOIN friends f ON u.user_id = f.friend_id
            WHERE f.user_id = ?""";
    private static final String GET_COMMON_FRIENDS_QUERY = """
            SELECT u.* FROM users u
            JOIN friends f1 ON u.user_id = f1.friend_id AND f1.user_id = ?
            JOIN friends f2 ON u.user_id = f2.friend_id AND f2.user_id = ?""";
    private static final String UNCONFIRMED_FRIEND_QUERY = """
            UPDATE friends
            SET status = 'UNCONFIRMED'
            WHERE user_id = ? AND friend_id = ?""";
    private static final String REMOVE_USER_BY_ID_QUERY = """
            DELETE FROM users
            WHERE user_id = ?
            """;

    private final JdbcTemplate jdbc;
    private final FilmDbStorage filmStorage;

    public UserDbStorage(JdbcTemplate jdbc, UserRowMapper userRowMapper, FilmDbStorage filmStorage) {
        super(jdbc, userRowMapper);
        this.jdbc = jdbc;
        this.filmStorage = filmStorage;
    }

    @Override
    public List<User> getUsers() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public User getUserById(Long id) {
        return findOne(FIND_BY_ID_QUERY, id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + id + " не найден"));
    }

    public Optional<User> findByEmail(String email) {
        return findOne(FIND_BY_EMAIL_QUERY, email);
    }

    @Override
    public User addUser(User user) {
        validateUser(user);

        long id = insert(INSERT_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday());
        user.setId(id);

        return user;
    }

    @Override
    public User updateUser(User user) {
        getUserById(user.getId());
        validateUser(user);

        update(UPDATE_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId());

        return user;
    }

    public void addFriend(Long userId, Long friendId) {
        jdbc.update(ADD_FRIEND_QUERY, userId, friendId, "UNCONFIRMED");
        boolean hasReverseRequest = jdbc.queryForObject(
                "SELECT COUNT(*) FROM friends WHERE user_id = ? AND friend_id = ?",
                Integer.class,
                friendId, userId) > 0;
        if (hasReverseRequest) {
            jdbc.update(CONFIRM_FRIEND_QUERY, userId, friendId);
            jdbc.update(CONFIRM_FRIEND_QUERY, friendId, userId);
        }
    }

    public void removeFriend(Long userId, Long friendId) {
        getUserById(userId);
        getUserById(friendId);

        jdbc.update(REMOVE_FRIEND_QUERY,
                userId, friendId);
        jdbc.update(UNCONFIRMED_FRIEND_QUERY, friendId, userId);
    }

    public List<User> getFriends(Long userId) {
        getUserById(userId);
        return jdbc.query(GET_FRIENDS_QUERY, mapper, userId);
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        getUserById(userId);
        getUserById(otherId);
        return jdbc.query(GET_COMMON_FRIENDS_QUERY, mapper, userId, otherId);
    }

    public void deleteUserById(Long id) {
        getUserById(id);
        update(REMOVE_USER_BY_ID_QUERY, id);
    }

    public List<Film> showRecommendations(Long userId) {
        getUserById(userId);
        return filmStorage.getRecommendations(userId);
    }

    private void validateUser(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        if (findByEmail(user.getEmail())
                .filter(u -> !u.getId().equals(user.getId()))
                .isPresent()) {
            throw new ValidationException("Электронная почта уже существует");
        }
    }
}