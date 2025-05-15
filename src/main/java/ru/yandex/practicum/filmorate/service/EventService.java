package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.EventRepository;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.enums.EventOperation;
import ru.yandex.practicum.filmorate.model.enums.EventType;

import java.util.List;


@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;

    public void addEvent(Long userId, EventType eventType, EventOperation operation, Long entityId) {
        eventRepository.addEvent(userId, eventType, operation, entityId);
    }

    public List<Event> getUserFeed(Long userId) {
        return eventRepository.getUserFeed(userId);
    }
}
