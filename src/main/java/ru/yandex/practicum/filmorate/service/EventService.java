package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.EventRepository;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.enums.EventOperation;
import ru.yandex.practicum.filmorate.model.enums.EventType;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;

    public void addEvent(Long userId, EventType eventType, EventOperation operation, Long entityId) {
        log.info("Добавление события");
        eventRepository.addEvent(userId, eventType, operation, entityId);
    }

    public List<Event> getUserFeed(Long userId) {
        log.info("Получение ленты для пользователя с ID {} на уровне сервиса", userId);
        return eventRepository.getUserFeed(userId);
    }
}
