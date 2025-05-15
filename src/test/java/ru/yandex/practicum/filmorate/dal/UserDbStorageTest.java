package ru.yandex.practicum.filmorate.dal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dal.mappers.*;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import({UserDbStorage.class,
        UserRowMapper.class,
        FilmDbStorage.class,
        FilmRowMapper.class,
        MpaRatingRepository.class,
        GenreRepository.class,
        MpaRowMapper.class,
        GenreRowMapper.class,
        DirectorRepository.class,
        DirectorRowMapper.class})
class UserDbStorageTest {

    @Autowired
    private UserDbStorage userStorage;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("test@mail.ru");
        testUser.setLogin("testLogin");
        testUser.setBirthday(LocalDate.of(2005, 6, 19));
    }

    @Test
    void addAndFindUserById() {
        User addedUser = userStorage.addUser(testUser);
        User foundUser = userStorage.getUserById(addedUser.getId());

        assertThat(foundUser)
                .isNotNull()
                .hasFieldOrPropertyWithValue("email", "test@mail.ru")
                .hasFieldOrPropertyWithValue("login", "testLogin");
    }

    @Test
    void updateUser() {
        User addedUser = userStorage.addUser(testUser);
        addedUser.setName("Updated Name");

        userStorage.updateUser(addedUser);
        User updatedUser = userStorage.getUserById(addedUser.getId());

        assertThat(updatedUser.getName()).isEqualTo("Updated Name");
    }

    @Test
    void addAndGetFriends() {
        User user1 = userStorage.addUser(testUser);

        User user2 = new User();
        user2.setEmail("friend@mail.ru");
        user2.setLogin("friendLogin");
        user2.setBirthday(LocalDate.of(2005, 6, 19));
        user2 = userStorage.addUser(user2);

        userStorage.addFriend(user1.getId(), user2.getId());
        List<User> friends = userStorage.getFriends(user1.getId());

        assertThat(friends).hasSize(1);
    }
}
