package ru.yandex.practicum.filmorate.model.event;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Event {
    @Positive
    private long timestamp;
    @Positive
    private Long userId;
    @NotNull
    private EventType eventType;
    @NotNull
    private Operation operation;
    @Positive
    private Long eventId;
    @Positive
    private Long entityId;
}