package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.EventRowMapper;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.enums.EventOperation;
import ru.yandex.practicum.filmorate.model.enums.EventType;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class EventRepository {
    private static final String INSERT_EVENT_QUERY =
            "INSERT INTO events (timestamp, user_id, event_type, operation, entity_id) VALUES (?, ?, ?, ?, ?)";
    private static final String GET_EVENTS_QUERY =
            "SELECT * FROM events WHERE user_id = ? ORDER BY timestamp";


    private final JdbcTemplate jdbc;
    private final EventRowMapper eventRowMapper;
    private final UserDbStorage userStorage;


    public void addEvent(Long userId, EventType eventType, EventOperation operation, Long entityId) {
        jdbc.update(INSERT_EVENT_QUERY,
                System.currentTimeMillis(),
                userId,
                eventType.name(),
                operation.name(),
                entityId);
    }

    public List<Event> getUserFeed(Long userId) {
        userStorage.getUserById(userId);
        return jdbc.query(GET_EVENTS_QUERY, eventRowMapper, userId);
    }
}


