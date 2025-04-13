package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.event.Event;
import ru.yandex.practicum.filmorate.model.event.EventType;
import ru.yandex.practicum.filmorate.model.event.Operation;
import ru.yandex.practicum.filmorate.storage.interfaces.EventStorage;
import ru.yandex.practicum.filmorate.storage.mappers.EventRowMapper;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class EventDbStorage implements EventStorage {
    private final JdbcTemplate jdbcTemplate;
    private final EventRowMapper eventRowMapper;

    public void addEvent(EventType eventType, Operation operation, Integer userId, Integer entityId) {
        long timestamp = Instant.now().toEpochMilli();
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("event")
                .usingGeneratedKeyColumns("event_id");

        simpleJdbcInsert.execute(Map.of("timestamp", timestamp,
                "user_id", userId,
                "eventType", eventType.name(),
                "operation", operation.name(),
                "entity_id", entityId));
    }

    public List<Event> getEventFeed(Integer userId) {
        String sql = "SELECT * FROM event WHERE user_id = ? ORDER BY timestamp";
        return jdbcTemplate.query(sql, eventRowMapper, userId);
    }

}